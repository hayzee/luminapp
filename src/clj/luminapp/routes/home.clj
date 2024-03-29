(ns luminapp.routes.home
  (:require
    [luminapp.layout :as layout]
    [luminapp.db.core :as db]
    ;    [clojure.java.io :as io]
    [luminapp.middleware :as middleware]
    [ring.util.response :refer [redirect]]
    [ring.util.http-response :as response]
    [struct.core :as st]))

(def ^:private message-schema
  [
  ;Not required if html element is "disabled" on home.html.
  ;[:name
  ;  st/required
  ;  st/string]
   [:message
    st/required
    st/string
    {:message  "message must contain at least 10 characters"
     :validate (fn [msg] (>= (count msg) 10))}]])


(defn- home-page [{:keys [flash] :as request}]
  (layout/render
    request
    "home.html"
    (merge
      {:messages (db/get-messages)}
      (:session request)
      (select-keys flash [:name :message :errors]))))


(defn- validate-message [params]
  (first (st/validate params message-schema)))


(defn- save-message! [{:keys [params session]}]
  ;  (db/save-message! params)
  ;  (response/found "/"))
  (if-let [errors (validate-message params)]
    (-> (response/found "/")
        (assoc :flash (assoc params :errors errors)))
    (do
      (db/save-message! (merge session params))
      (response/found "/"))))


(defn- about-page [request]
  (layout/render request "about.html"))


(defn home-routes []
   [""
    {:middleware [middleware/wrap-login
                  middleware/wrap-csrf
                  middleware/wrap-formats
                  middleware/wrap-nocache
                  ]}
    ["/" {:get home-page}]
    ["/message" {:post save-message!}]
    ["/about" {:get about-page}]
    ]
  )

