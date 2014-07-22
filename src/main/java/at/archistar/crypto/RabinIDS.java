package at.archistar.crypto;

import at.archistar.crypto.data.Share;
import at.archistar.crypto.exceptions.ReconstructionException;
import at.archistar.crypto.exceptions.WeakSecurityException;
import at.archistar.crypto.math.CustomMatrix;
import at.archistar.crypto.math.GF256Polynomial;
import at.archistar.crypto.math.PolyGF256;

/**
 * @author Elias Frantar <i>(improved Exception handline)</i>
 * @author Andreas Happe <andreashappe@snikt.net>
 * @author Fehrenbach Franca-Sofia
 * @author Thomas Loruenser <thomas.loruenser@ait.ac.at>
 */
public class RabinIDS extends SecretSharing {
	public RabinIDS(int n, int k) throws WeakSecurityException {
        super(n, k);
    }

    private boolean checkForZeros(int[] a) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Share[] share(byte[] data) {
        //Create shares
        Share shares[] = new Share[n];
        for (int i = 0; i < n; i++) {
            shares[i] = new Share(i + 1, (data.length + k - 1) / k, data.length, Share.Type.REED_SOLOMON);
        }

        int a[] = new int[k];

        int fillPosition = 0;
        for (int i = 0; i < data.length; i += k) {

            //Let k coefficients be the secret in this polynomial
            for (int j = 0; j < k; j++) {
                if ((i + j) < data.length) {
                    a[j] = (data[i + j] < 0) ? data[i + j] + 256 : data[i + j];
                    assert (a[j] >= 0 && a[j] <= 255);
                } else {
                    a[j] = 0;
                }
            }

            GF256Polynomial poly = new GF256Polynomial(a);

            //Calculate the share for this (source)byte for every share
            for (int j = 0; j < n; j++) {

                if (checkForZeros(a)) {
                    System.err.println("all a coefficients are zero");
                    System.err.println("i: " + i + " data.length: " + data.length);
                    shares[j].yValues[fillPosition] = 0;
                } else {
                    shares[j].yValues[fillPosition] = (byte) (poly.evaluateAt(shares[j].xValue) & 0xFF);
                }
            }
            fillPosition++;
        }

        return shares;
    }

    @Override
    public byte[] reconstruct(Share[] shares) throws ReconstructionException {
    	if (!validateShareCount(shares.length, k)) {
    		throw new ReconstructionException();
    	}
    	
    	try {
	        int xValues[] = new int[k];
	        byte result[] = new byte[shares[0].contentLength];
	
	        for (int i = 0; i < k; i++) {
	            xValues[i] = shares[i].xValue;
	        }
	
	        int w = 0;
	
	        CustomMatrix decodeMatrix = PolyGF256.erasureDecodePrepare(xValues);
	
	        for (int i = 0; i < shares[0].yValues.length; i++) {
	
	        	int yValues[] = new int[k];
	            for (int j = 0; j < k; j++) {
	            	yValues[j] = (shares[j].yValues[i] < 0) ? (shares[j].yValues[i] + 256) : shares[j].yValues[i];
	            }
	
	            if (checkForZeros(yValues)) {
	            	for (int x = 0; x < k && w < result.length; x++) {
	            		result[w++] = 0;
	                }
	           } else {
	                int resultMatrix[] = decodeMatrix.rightMultiply(yValues);
	
	                for (int j = resultMatrix.length - 1; j >= 0 && w < shares[0].contentLength; j--) {
	                	int element = resultMatrix[resultMatrix.length - 1 - j];
	                    result[w++] = (byte) (element & 0xFF);
	                }
	           }
	        }
	        return result;
    	} catch (Exception e) { // if anything goes wrong during reconstruction, throw a ReconstructionException
    		throw new ReconstructionException();
    	}
    }
}
