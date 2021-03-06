archistar-smc [![Build Status](https://travis-ci.org/Archistar/archistar-smc.png?branch=master)](https://travis-ci.org/Archistar/archistar-smc)
=============

Secure multiparty cryptography for the [Archistar secure multi-cloud Prototype](http://github.org/archistar/archistar-core).

Currently implemented algorithms
--------------------------------

* RabinIDS
* [Shamir's Secret Sharing](http://en.wikipedia.org/wiki/Shamir%27s_Secret_Sharing)
* [Krawczyk Secret Sharing Made Short](http://courses.csail.mit.edu/6.857/2009/handouts/short-krawczyk.pdf)
* [Rabin-Ben-Or Verifiable Secret Sharing](http://www.cse.huji.ac.il/course/2003/ns/Papers/RB89.pdf)
* [Cevallos (partially, MAC is missing)](http://www.algant.eu/documents/theses/cevallos.pdf)

Performance Numbers
-------------------

Rough observed performance numbers on a Intel Core i5-3570k, single core, Java 1.7, 256kByte Datasets:

```
ShamirPSS:                        share:  24854.369kByte/sec, combine: 16075.35kByte/sec

RabinIDS:                         share: 107789.474kByte/sec, combine: 53333.33kByte/sec

KrawczykCSS AES-CBC:              share:  50443.350kByte/sec, combine: 33684.21kByte/sec
KrawczykCSS AES-GCM:              share:  31411.043kByte/sec, combine: 24824.24kByte/sec
KrawczykCSS ChaCha:               share:  64402.516kByte/sec, combine: 41457.49kByte/sec

RabinBenOrRSS (AES, SHA256):      share:  17594.502kByte/sec, combine: 14927.11kByte/sec
RabinBenOrRSS (ChaCha, SHA256):   share:  19730.250kByte/sec, combine: 16677.52kByte/sec
RabinBenOrRSS (AES, Poly1305):    share:  30385.757kByte/sec, combine: 22629.83kByte/sec
RabinBenOrRSS (ChaCha, Poly1305): share:  34362.416kByte/sec, combine: 24674.70kByte/sec

CevallosUSRSS (AES/CBC, SHA256):  share:   4550.100kByte/sec, combine:   173.03kByte/sec
```

Basic Introduction to Secret Sharing
---------------------------------------------

Disclaimer: I'm a software developer and this should be seen as an very simplistic introduction to the used algorithms.

The goal of secret sharing is to split up a secret text into multiple parts so that a subset of those parts (called shares) can be used to reconstruct the original secret. The minimum amount of shares needed to reconstruct the secret should be configurable at 'split-up' time, i.e. it should be possible to split up a secret into 8 parts (shares) of which any 5 are needed to reconstruct the original data.

A basic example of Shamir's Secret Sharing should illustrate a possible contruction technique: let's assume that we want to split up '42' (the secret) into 5 parts, 3 of which are needed to reconstruct the original data ('42'). We will be using polynomials: to solve a polynomial of the third degree three points (x|y) are needed. So if we calculate 5 solutions for a polynomial of the third degree and distribute one solution to each participant each, any subset larger than 3 can solve the polynomial (and thus reconstruct the secret).

The generic form of a polynomial of the third degree is:
```
  y = a_0 * x^0 + a_1 * x^1 + a_2 * x^2
```

We choose a\_1 and a\_2 randomly (i.e. 1 and 2) and substitute a\_0 with our secret (42), thus creating the following equation:

``
 y = 42 + 1 * x + 2 * x^2
``

Now we calculate 5 x|y pairs, for example:

```
| x | y             | y  |
+---+---------------+----+
| 1 | 42 + 1 + 2*1  | 45 |
| 2 | 42 + 2 + 2*4  | 52 |
| 3 | 42 + 3 + 2*9  | 63 |
| 4 | 42 + 4 + 2*16 | 76 |
| 5 | 42 + 5 + 2*25 | 97 |
```

and distribute one point to each participant. For example User 1 retrieves x=1|y=45, User 2 retrieves x=2,y=52 and so on. No user can tell anything about the original secret.

To reconstruct the secret 3 participant have to exchange their information, in case of User 1, 2 and 3 this would yield the following equations:

```
 45 = a_0 + a_1 * 1 + a_2 * 1^2
 52 = a_0 + a_1 * 2+ a_2 * 2^2
 63 = a_0 + a_1 * 3 + a_2 * 3^2
```

We can use sage to solve this solution:

``` python
a0, a1, a2 = var("a0 a1 a2")
eq1 =  45 == a0 + a1 + a2
eq2 =  52 == a0 + 2*a1 + 4*a2
eq3 = 63 == a0 + 3*a1 + 9*a2
solve([eq1, eq2, eq3], a0, a1, a2)

-> [[a0 == 42, a1 == 1, a2 == 2]]
```

Through solving this a_0 can be calculated to be '42'. Voila, secret restored!

How to contribute/hack
----------------------

1. fork it
2. work on your new feature
3. run the testcases with `mvn test`
4. send me a pull request

Citing Archistar
----------------------

If you find Archistar useful for your work or if you use Archistar in a project, paper, website, etc., 
please cite the software as

T. Lorünser, A. Happe, D. Slamanig (2014). “ARCHISTAR – A framework for secure distributed storage”. GNU General Public License. http://ARCHISTAR.at
