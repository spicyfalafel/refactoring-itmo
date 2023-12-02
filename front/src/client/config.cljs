(ns client.config)

(def debug?
  ^boolean goog.DEBUG)

(def port 8080)

(def back-url (str "https://localhost:" port))
