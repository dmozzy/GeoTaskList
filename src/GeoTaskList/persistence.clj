(ns GeoTaskList.persistence
  (:require [appengine-magic.services.datastore :as ds]))


(ds/defentity Location [^:key locationId, user, ^:clj location])
(ds/defentity Task [^:key taskId, user, locationId, complete, ^:clj task])


;Save a location. -1 is used to signify no id passed in and nil
;should be substitued
(defn save-location [user location-map] 
  (let [input-id (Integer/parseInt (:locationId location-map))
        id (if (= input-id -1) nil input-id)]
    (ds/save! (Location. id user location-map))))

;Loads all locations. Retrieves the location field map for all locations
; and then adds in the locationId key value pair for each location.
(defn get-all-locations [user] 
  (map 
    (fn [k] 
      (assoc (:location k) :locationId (ds/key-id k)));This line sets the id onto the map at load time 
    (ds/query :kind Location :filter (= :user user))))

;Gets a map of locations keyed by the locationId value
(defn get-all-locations-map [user]
  (let [loaded-locations  
        (map 
          (fn [k] 
            (assoc (:location k) :locationId (ds/key-id k)));This line sets the id onto the map at load time 
          (ds/query :kind Location :filter (= :user user)))]
    (zipmap (map (fn [k] (:locationId k)) loaded-locations) loaded-locations)))
 
;Deletes a location by locationId
(defn delete-location [user locationId] 
  (let [location (ds/retrieve Location locationId)] 
    (if (= (:user location) user)      
      (ds/delete! location))))

(defn save-task [user task-map] 
  (let [input-id (Integer/parseInt (:taskId task-map))
        id (if (= input-id -1) nil input-id)
        locationId (:locationId task-map)]
    (ds/save! (Task. id user locationId "false" task-map))))

(defn complete-task [user taskId] 
  (let [task (ds/retrieve Task taskId)] 
    (if (= (:user task) user) 
      (ds/save! (assoc task :complete "true")))))
                                    

(defn get-all-tasks [user] 
  (let [locations 
        (get-all-locations-map user)]
    (map 
      (fn [k] 
        (let [task-map (:task k)
              taskLocationId (:taskLocationId task-map)
              location  (if (= nil taskLocationId) nil (get locations (Integer/parseInt taskLocationId)))] 
          (if (= location nil)
            task-map 
            (assoc 
              task-map 
              :taskId (ds/key-id k) 
              :taskLat (:locationLat location)
              :taskLng (:locationLng location)))))
      (ds/query :kind Task :filter  [(= :user user) (= :complete "false")]))))


