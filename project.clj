(defproject alpaca "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.4.474"]
                 [http-kit "2.2.0"]
                 [org.clojure/data.json "0.2.6"]]
  :profiles {:dev {:dependencies [[com.github.tomakehurst/wiremock "2.14.0"]]}})
