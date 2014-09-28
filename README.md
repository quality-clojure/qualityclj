# Quality Clojure

FIXME

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen

You will also need an installation of Datomic Free

## Running

To start a web server for the application, run (in 3 terminals):

    bin/transactor config/samples/free-transactor-template.properties
    
    lein figwheel

    lein ring server

## License

Copyright © 2014 FIXME
