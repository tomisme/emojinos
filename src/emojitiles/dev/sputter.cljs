(ns emojitiles.dev.sputter
  (:require
   [sputter.vm])
  (:require-macros
   [devcards.core :refer [defcard]]))

(defcard "Where's sputter?")

(defcard a
  (sputter.vm/disassemble "0x60ff60ee01600202"))
