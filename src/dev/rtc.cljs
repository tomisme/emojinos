(ns dev.rtc
  (:require
   [re-frame.core :as rf :refer [reg-event-db reg-sub dispatch subscribe]]
   [reagent.core :as r])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg]]))

(def offer-input-val
 (r/atom ""))

(def answer-input-val
 (r/atom ""))

(reg-sub
 :local-description
 (fn [db _]
   (-> db :local :description)))

(reg-sub
 :remote-description
 (fn [db _]
   (-> db :remote :description)))

(reg-event-db
 :got
 (fn [db [_ a b]]
   (let [_ (js/console.log a b)]
     db)))

(reg-event-db
  :got-remote-description
  (fn [db [_ desc]]
    (let [
          local (get-in db [:local :connection])
          remote (get-in db [:remote :connection])
          _ (dispatch [:got "remote description sdp:" (.-sdp desc)])]
      ; (.setLocalDescription remote desc)
      (.setRemoteDescription local desc)
      (assoc-in db [:remote :description] (.-sdp desc))
      db)))

(reg-event-db
  :got-local-description
  (fn [db [_ desc]]
    (let [
          local (get-in db [:local :connection])
          ; remote (get-in db [:remote :connection])
          _ (dispatch [:got "local description sdp:" (.-sdp desc)])]
      (.setLocalDescription local desc)
      ; (.setRemoteDescription remote desc)
      ; (.createAnswer remote #(dispatch [:got-remote-description %]) #())
      (assoc-in db [:local :description] (.-sdp desc)))))

(reg-event-db
 :p2-handle-offer
 (fn [db _]
   (let [offer-text @offer-input-val
         desc (js/RTCSessionDescription. #js {:type "offer"
                                              :sdp offer-text})]
     (dispatch [:got-remote-description desc])
     (dispatch [:p2-create-answer])
     db)))

(reg-event-db
 :p2-create-answer
 (fn [db _]
   (let [remote (get-in db [:remote :connection])]
     (.createAnswer remote #(dispatch [:got-local-description %]) #()))))

(reg-event-db
 :p1-handle-answer
 (fn [db [_ desc]]
   (let [])))

(reg-event-db
 :p1-create-offer
 (fn [db _]
   (let [
         local (get-in db [:local :connection])
         ; remote (get-in db [:remote :connection])
         success-fn #(dispatch [:got-local-description %])
         failure-fn #(dispatch [:got "failed to create offer:" %])]
      (.createOffer local success-fn failure-fn)
      db)))

(reg-event-db
  :p1-create-channel
  (fn [db _]
    (let [local (get-in db [:local :connection])
          ; remote (get-in db [:remote :connection])
          send-channel (.createDataChannel local "send-data-channel" #js {:ordered true})]
      (set! (.-onopen send-channel) #(dispatch [:got "send channel opened:" %]))
      (set! (.-onclose send-channel) #(dispatch [:got "send channel closed:" %]))
      (set! (.-onerror send-channel) #(dispatch [:got "send channel error:" %]))
      (set! (.-onmessage send-channel) #(dispatch [:got "send channel message:" %]))
      ; (set! (.-ondatachannel remote) #(dispatch [:got "got remote channel" %]))
      (dispatch [:got "new send channel:" send-channel])
      (dispatch [:got "send channel state:" (.-readyState send-channel)])
      (dispatch [:p1-create-offer])
      (assoc-in db [:local :channel] send-channel))))

(reg-event-db
  :p1-start
  (fn [db _]
    (let [cfg {"iceServers" [{"urls" "stun:stun.l.google.com:19302"}]}
          local-conn (js/RTCPeerConnection. (clj->js cfg))]
          ; remote-conn (js/RTCPeerConnection. (clj->js cfg))]
      ; (set! (.-onicecandidate local-conn) #(dispatch [:got "local ice candidate:" %]))
      ; (set! (.-onicecandidate remote-conn) #(dispatch [:got "remote ice candidate:" %]))
      (dispatch [:p1-create-channel])
      (-> db
        (assoc-in [:local :connection] local-conn)))))
        ; (assoc-in [:remote :connection] remote-conn)))))

(defn text-input-component
  [val-atom]
  [:input {:type :text
           :value @val-atom
           :on-change #(reset! val-atom (-> % .-target .-value))}])

(defn test-component
  []
  [:div
   [:p
    [:button {:on-click #(dispatch [:p1-start])}
      "P1 start"]]
   [:p "Local Description:"]
   [:p @(subscribe [:local-description])]
   [:p "Remote Description:"]
   [:p @(subscribe [:remote-description])]
   [:p "P2, paste offer below:"]
   [:p
    [text-input-component offer-input-val]]
   [:p
    [:button {:on-click #(dispatch [:p2-handle-offer])}
      "P2 handle offer"]]
   [:p "P1, paste answer below:"]
   [:p
    [text-input-component answer-input-val]]
   [:p
    [:button {:on-click #(dispatch [:p1-handle-answer])}
      "P1 handle answer"]]
   [:p
    [:button {:on-click #(dispatch [:send-data "hello!"])}
      "say hello"]]])

(defcard-rg test
  [test-component])
