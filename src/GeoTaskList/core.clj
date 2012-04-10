(ns GeoTaskList.core
  (:require [appengine-magic.core :as ae]
            [compojure.route :as route])
  (:use [compojure.core] 
        [cheshire.core :as json] 
        [ring.util.response :as ring-response]
        [compojure.handler :as comp-handler]
        [GeoTaskList.persistence :as persist]))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string data)})


(defroutes geotasklist-main-handler
           ;Get All Locations Routes
           (GET "/locations" []
                (json-response {:locations (get-all-locations)}))

           ;Delete Location Route           
           (POST "/delete-location" {params :params} 
                 (json-response (delete-location (Integer/parseInt (:locationId params)))))

           ;Update Location Route                                 
           (POST "/location" {params :params} 
                 (json-response {:locationId (persist/save-location params)}))                         

           (GET "/tasks" []
                (json-response {:tasks (get-all-tasks)}))           
           
           (POST "/task" {params :params} 
                 (json-response {:taskId (persist/save-task params)}))                         
           
                      
           ;Static Resources
           (route/resources "/")
           (route/not-found "Page not found"))


(def app (comp-handler/api geotasklist-main-handler))

(ae/def-appengine-app geotasklist-app (var app))

(comment
  (ae/serve geotasklist-app)
  (ae/stop)
  )