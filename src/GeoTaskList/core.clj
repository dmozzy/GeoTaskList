(ns GeoTaskList.core
  (:require [appengine-magic.core :as ae]
            [compojure.route :as route])
  (:use [compojure.core] 
        [cheshire.core :as json] 
        [ring.util.response :as ring-response]))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string data)})


(def test-location-1 {:name "Hardware" :lat -31.957242 :lng 115.931422})
(def test-location-2 {:name "Mall" :lat -31.964779 :lng 115.935241})
(def test-locations (list test-location-1 test-location-2))

(defroutes geotasklist-main-handler
           (GET "/locations" []
                (json-response {:locations test-locations}))
           
           (GET "/location/:id" [id]
                (json-response (nth test-locations id)))
           (route/resources "/")
           (route/not-found "Page not found"))

(ae/def-appengine-app geotasklist-app #'geotasklist-main-handler)

(comment
  (ae/serve geotasklist-app)
  (ae/stop)
  )