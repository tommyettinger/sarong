# Recommended Generators in Sarong

This document is a work in progress, but when it's done it should be stable documentation
for the known properties of the recommended generators in this library. It is split into
first-party generators (those where I understand the internals because I wrote them) and
third-party generators (which I may only partly understand, so I'll refer to the original
author's page or paper and only leave commentary if that material is missing something).

## First-Party Generators

Table of Notable Properties

| Generator Name | PractRand | Min. Cycle     | Max. Cycle     | Possible Seeds | Possible Streams | Step-Back     |
|----------------|-----------|----------------|----------------|----------------|------------------|---------------| 
| BasicRandom64  | 1 unusual | 2<sup>64</sup> | 2<sup>64</sup> | 2<sup>64</sup> | 1                | Possible, NYI |
| BellRNG        | 1 unusual | 2<sup>128</sup> - 2<sup>64</sup> | 2<sup>128</sup> - 2<sup>64</sup> |  2<sup>128</sup> - 2<sup>64</sup> | 1                | Not Feasible |
| BrightRNG      | none      | 2<sup>64</sup> | 2<sup>64</sup> | 2<sup>64</sup> | 1                | Step/Jump Anywhere |
| DirkRNG        | 1 unusual | 2<sup>64</sup> | 2<sup>64</sup> | 2<sup>64</sup> | 1                | Step/Jump Anywhere |
| DiverRNG       | 1 unusual | 2<sup>64</sup> | 2<sup>64</sup> | 2<sup>64</sup> | 1                | Step/Jump Anywhere |
| LinnormRNG     | 1 unusual | 2<sup>64</sup> | 2<sup>64</sup> | 2<sup>64</sup> | 1                | Possible Internally, NYI |
| MizuchiRNG     | none      | 2<sup>64</sup> | 2<sup>64</sup> | 2<sup>64</sup> | 2<sup>63</sup>   | Possible Internally, NYI |
| PelicanRNG     | none      | 2<sup>64</sup> | 2<sup>64</sup> | 2<sup>64</sup> | 1                | Step/Jump Anywhere |
| PulleyRNG      | 1 unusual | 2<sup>64</sup> | 2<sup>64</sup> | 2<sup>64</sup> | 1                | Step/Jump Anywhere |

### BasicRandom64
This is meant to make generating decent-quality numbers easier by extending java.util.Random
with a matching API, but it cannot produce all long results and is not equidistributed.

