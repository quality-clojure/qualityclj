# Quality Clojure

Use a number of existing tools like `kibit`, `eastwood`, and `bikeshed` to get feedback about a particular library. When it's finished, you'll be able to point to a github repository, and the service will suck it in. From that point, there are a few things that will happen:

* import the repository into datomic
* run kibit, import comments into datomic
* run eastwood, import comments into datomic
* run bikeshed, import comments into datomic
* run pygments on each source file to output a static html file for display 

At this point, you can access the repository at `/repo/<github user or org>/<repo-name>/` and get a listing of the files available to view, as well as overall stats for the repository. From here, you can drill down and view individual files and the notes and comments associated with each one.

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

For syntax highlighting, you'll need [Pygments][2] installed and
available on your path.

You will also need an installation of [Datomic Free][3]

[1]: https://github.com/technomancy/leiningen
[2]: http://pygments.org/
[3]: https://my.datomic.com/downloads/free

## Running

First, start the Datomic transactor:

    bin/transactor config/samples/free-transactor-template.properties

Then, start figwheel, which will send updated compiled javascript to the browser:

    lein figwheel

To start a web server for the application (in development):

    lein ring server

To create a production uberjar:

    lein ring uberjar

To run in a production environment, make sure to pass in the URI for
the production database:

    java -Ddb.url=datomic:free://localhost:4334/<db-name> -jar
    standalone.jar
    
## Logging

We are using [Timbre](https://github.com/ptaoussanis/timbre) for logging. 

If you have a namespace where you want logging or profiling, insert the following to use Timbre in the namespace:

```clojure
(ns sad-without-logging (:require [taoensso.timbre :as timbre])) ;; The ns needing some love
(timbre/refer-timbre) ;; Provides useful Timbre aliases in the ns
```

And you're good to go. Here is a simple example of logging to get you going:

```clojure
(defn can-i-has-logging []
     (debug "startin difficult computashuns")
     (spy :info (+ 0 1 2 3 4 5 6))
     (spy :warn "u liek teh math" (* 9 8 7)))
     
; 2014-Oct-09 21:35:51 -0400 cat-pc DEBUG [qualityclj.repl] - startin difficult computashuns
; 2014-Oct-09 21:35:51 -0400 cat-pc INFO [qualityclj.repl] - (+ 0 1 2 3 4 5 6) 21
; 2014-Oct-09 21:35:51 -0400 cat-pc WARN [qualityclj.repl] - u liek the math 504
;=> 504
```

Check out [Timbre](https://github.com/ptaoussanis/timbre) for more information and how to use it's great logging and profiling capabilities.
    
## TODO
Check out the [issues!](https://github.com/jcsims/qualityclj/issues)

## License

Copyright © 2014 Chris Sims and Scott Bauer
