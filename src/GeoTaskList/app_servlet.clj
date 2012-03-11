(ns GeoTaskList.app_servlet
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use GeoTaskList.core)
  (:use [appengine-magic.servlet :only [make-servlet-service-method]]))


(defn -service [this request response]
  ((make-servlet-service-method GeoTaskList-app) this request response))
