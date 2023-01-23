(ns kee-frame-repro.main
  (:require
    [org.httpkit.server :as httpkit]
    [clojure.java.io :as io]
    [clojure.string :as str]))

(def page
  (str
    "<html>"
      "<head>"
        "<title>Kee-Frame Bug Reproduction</title>"
      "</head>"
      "<body>"
        "<div id=\"app\"></div>"
        "<script src=\"/js/compiled/app.js\" type=\"text/javascript\"></script>"
      "</body>"
    "</html>"))

(defn try-get-resource
  [{:keys [uri]}]
  (let [path (str "./resources/public" uri)
        file (io/file path)]
    (println "Looking for " path)
    (if (.exists file)
      {:status 200
       :body (slurp file)}
      {:status 404
       :body "Not Found"})))
  
(defn handler
  [{:keys [uri] :as req}]
  (try
    (cond
      (str/starts-with? uri "/js/compiled")
      (try-get-resource req)

      "/"
      {:status 200
       :body page}
      
      :else
      {:status 308
       :headers {"Location" "/"}})
    (catch Exception e
      (println e)
      {:status 500})))

(defonce stop-fn (atom nil))

(defn restart []
  (swap! stop-fn
         (fn [stop-fn]
           (when stop-fn
             (println "Stopping server")
             (stop-fn)
             (println "Server stopped"))
           (println "Starting server on http://localhost:8080")
           (httpkit/run-server
             handler
             {:port 8080}))))

(defn -main []
  (restart))

(comment  
  (@stop-fn)
  (restart))
