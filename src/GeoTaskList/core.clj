(ns GeoTaskList.core
  (:require [appengine-magic.core :as ae]
            [compojure.route :as route]
            [appengine-magic.services.user :as aeu]             
            )
  (:use [compojure.core] 
        [cheshire.core :as json] 
        [ring.util.response :as ring-response]
        [compojure.handler :as comp-handler]
        [GeoTaskList.persistence :as persist]))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string data)})

(defn require-login [application] 
  (fn [request] 
    (if 
      (aeu/user-logged-in?)  
      (application request)
      (ring-response/redirect 
        (aeu/login-url))
      )))

(defn getEmail [] 
  (.getEmail (aeu/current-user)))
(defroutes geotasklist-main-handler
           ;Get All Locations Routes
           (GET "/" [] (ring-response/redirect "/index.html"))
           
           (GET "/locations" []
                (json-response {:locations (persist/get-all-locations (getEmail))}))

           ;Delete Location Route           
           (POST "/delete-location" {params :params} 
                 (json-response (persist/delete-location (getEmail) (Integer/parseInt (:locationId params)))))

           ;Update Location Route                                 
           (POST "/location" {params :params} 
                 (json-response {:locationId (persist/save-location (getEmail) params)}))                         

           (GET "/tasks" []
                (json-response {:tasks (get-all-tasks (getEmail))}))           
           
           (POST "/task" {params :params} 
                 (json-response {:taskId (persist/save-task (getEmail) params)}))                         
           
           (POST "/complete-task" {params :params} 
                 (json-response {:taskId (persist/complete-task (getEmail) (Integer/parseInt (:taskId params)))}))

           ;Static Resources           
           (route/resources "/")
           (route/not-found "Page not found"))


(def app (require-login (comp-handler/api geotasklist-main-handler)))

(ae/def-appengine-app geotasklist-app (var app))

(comment
  (ae/serve geotasklist-app)
  (ae/stop)
  )