# ![](logo.png) Quality Clojure
[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/quality-clojure/qualityclj?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Stories in Ready](https://badge.waffle.io/quality-clojure/qualityclj.png?label=ready&title=Ready)](https://waffle.io/quality-clojure/qualityclj)
[![Build Status](https://semaphoreapp.com/api/v1/projects/5e9e023a-ac2a-44b8-9b2e-dfba32d2f70f/272099/badge.png)](https://semaphoreapp.com/jcsims/qualityclj)

Use a number of existing tools like [Kibit], [Eastwood], and [Bikeshed] to get feedback about a particular library. When it's finished, you'll be able to point to a github repository, and the service will suck it in. From that point, there are a few things that will happen:

* import the repository into Datomic
* run kibit, import comments into Datomic
* run eastwood, import comments into Datomic
* run bikeshed, import comments into Datomic
* run pygments on each source file to output a static html file for display

At this point, you can access the repository at `/repo/<github user or org>/<repo-name>/` and get a listing of the files available to view, as well as overall stats for the repository. From here, you can drill down and view individual files and the notes and comments associated with each one.

## Prerequisites

You will need:

* [Clojure] - 1.6.0 or above.
* [Leiningen] - 1.7.0 or above.
* [Pygments] - For syntax highlighting; will need to be available on your path.
* [Datomic] - Currently only tested on [Datomic Free]. Try this [setup tutorial][Datomic setup tutorial] if you have trouble. Not necessary to setup Datomic Free if you are only developing.

## Setting up a Production Instance

**Please keep in mind Quality Clojure is in development**

First, create a production uberjar from the command line:

    lein with-profile production uberjar

Place the resulting uberjar where you want [Quality Clojure] to run.

In the root of your Datomic directory, start the Datomic transactor (you did remember to install [Datomic], didn't you?):

    bin/transactor config/samples/free-transactor-template.properties

Finally, run the jar. Make sure to pass in the URI for the production database:

    java -Ddb.url=datomic:free://localhost:4334/<db-name> -jar standalone.jar

With any luck, everything should be up, running correctly, and viewable from http://localhost:3000/.

## Development

### Datomic

For development, we use an in-memory version of [Datomic] instead of more robust options, such as running the transactor with the free storage protocol. This in-memory version is already part of Quality Clojure and no installation is required by the developer to set it up. How great is that?!

This means whatever is sent to the database does not persist beyond the repl or program it is launched in.

It is important to ensure the database is running and populated with schemas before trying to use it. In fact, we thought it was so important, we made a function to do just that! Simply call `(qualityclj.models.db/ensure-db)` to initialize the database.

Alternatively, `(qualityclj.repl/start-server)` will, among other things, ensure the database is ready.

### Webserver

To start the application web server from the `qualityclj.repl` namespace:

    qualityclj.repl=> (start-server)

For a tighter thought-code-feedback loop when working on the client side, start [figwheel]. In a nutshell, figwheel updates compiled [ClojureScript] to the browser so you won't have to manually reload the page constantly:

    lein figwheel

### Testing

Coming soon, I promise ;-)

The plan is to use [clojure.test] once we have a core not in constant flux.

If you are contributing, please make sure that there are tests to cover any new
functionality introduced, or bugs fixed.

The ClojureScript tests require `phantomjs` to be on your PATH. Downloads can be found on the 
[homepage][phantomjs].

### Logging

We are using [Timbre] for logging.

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

Check out [Timbre] for more information and how to use it's great logging and profiling capabilities.

## Contributing/TODO
Check out the [issues]!

## License

Copyright © 2014 Chris Sims and Scott Bauer

[Clojure]: http://clojure.org/
[ClojureScript]: https://github.com/clojure/clojurescript
[Clojure.test]: https://clojure.github.io/clojure/clojure.test-api.html
[Pygments]: http://pygments.org/
[Datomic]: https://www.datomic.com/
[Datomic Free]: https://my.datomic.com/downloads/free
[Datomic setup tutorial]: http://docs.datomic.com/getting-started.html
[Timbre]: https://github.com/ptaoussanis/timbre
[Figwheel]: https://github.com/bhauman/lein-figwheel
[Leiningen]: https://github.com/technomancy/leiningen
[Kibit]: https://github.com/jonase/kibit
[Eastwood]: https://github.com/jonase/eastwood
[Bikeshed]: https://github.com/dakrone/lein-bikeshed
[Quality Clojure]: https://github.com/quality-clojure/qualityclj
[Issues]: https://github.com/jcsims/qualityclj/issues
[phantomjs]: http://phantomjs.org/
