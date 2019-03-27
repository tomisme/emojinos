(ns dev.rtc
  (:require [rtc.core :as rtc])
  (:require-macros [devcards.core :refer [defcard defcard-rg]]))

(defcard-rg test
  [rtc/test-component])
