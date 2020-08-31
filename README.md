High-level Petri games
======================
A framework for the synthesis of distributed systems modeled with high-level Petri games. 

Contains:
---------
- data structures for symmetric Petri games,
- data structures for the corresponding two-player games over a finite graph (cp. [Acta'20](https://doi.org/10.1007/s00236-020-00368-5)):
  1) explicit version, 
  2) low-level version (exploiting the symmetries of the net),
  3) high-level version (exploiting the symmetries of the net),
  4) bdd version (partially exploiting the symmetries of the net),
- solving algorithms for all the approaches,
- converter to generate a low-level Petri game from a high-level one,
- generators for example high-level Petri games (cp. [ArXiv'19](http://arxiv.org/abs/1904.05621)).

Integration:
------------
This modules can be used as separate library and
- is integrated in: [adam](https://github.com/adamtool/adam), [adamsynt](https://github.com/adamtool/adamsynt),
- contains the packages: highLevel,
- depends on the repos: [libs](https://github.com/adamtool/libs), [framework](https://github.com/adamtool/framework), [synthesizer](https://github.com/adamtool/synthesizer).

Related Publications:
---------------------
- _Manuel Gieseking, Ernst-Rüdiger Olderog, Nick Würdemann:_
  [Solving high-level Petri games](https://doi.org/10.1007/s00236-020-00368-5). Acta Informatica 57(3-5): 591-626 (2020)
- _Manuel Gieseking, Ernst-Rüdiger Olderog:_
  [High-Level Representation of Benchmark Families for Petri Games](http://arxiv.org/abs/1904.05621). CoRR abs/1904.05621 (2019)
