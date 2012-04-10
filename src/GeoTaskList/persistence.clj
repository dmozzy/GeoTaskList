(ns GeoTaskList.persistence
  (:require [appengine-magic.services.datastore :as ds]))


(ds/defentity Location [^:key locationId, ^:clj location])
(ds/defentity Task [^:key taskId, locationId, ^:clj task])


;Save a location. -1 is used to signify no id passed in and nil
;should be substitued
(defn save-location [location-map] 
  (let [input-id (Integer/parseInt (:locationId location-map))
        id (if (= input-id -1) nil input-id)]
    (ds/save! (Location. id location-map))))

;Loads all locations. Retrieves the location field map for all locations
; and then adds in the locationId key value pair for each location.
(defn get-all-locations [] 
  (map 
    (fn [k] 
      (assoc (:location k) :locationId (ds/key-id k)));This line sets the id onto the map at load time 
    (ds/query :kind Location)))

;Gets a map of locations keyed by the locationId value
(defn get-all-locations-map []
  (let [loaded-locations  
        (map 
          (fn [k] 
            (assoc (:location k) :locationId (ds/key-id k)));This line sets the id onto the map at load time 
          (ds/query :kind Location))]
    (zipmap (map (fn [k] (:locationId k)) loaded-locations) loaded-locations)))
 
;Deletes a location by locationId
(defn delete-location [locationId] 
  (ds/delete! 
    (ds/retrieve Location locationId)))

(defn save-task [task-map] 
  (let [input-id (Integer/parseInt (:taskId task-map))
        id (if (= input-id -1) nil input-id)
        locationId (:locationId task-map)
        ]
    (ds/save! (Task. id locationId task-map))))

(defn get-all-tasks [] 
  (let [locations 
        (get-all-locations-map)]
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
      (ds/query :kind Task))))


