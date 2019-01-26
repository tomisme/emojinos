(ns dev.rtc
  (:require [re-frame.core :as rf :refer [reg-event-db reg-sub dispatch subscribe]])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg]]))

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
      ; (.setRemoteDescription local desc)
      (assoc-in db [:remote :description] (.-sdp desc))
      db)))

(reg-event-db
  :got-local-description
  (fn [db [_ desc]]
    (let [
          local (get-in db [:local :connection])
          remote (get-in db [:remote :connection])
          _ (dispatch [:got "local description sdp:" (.-sdp desc)])]
      ; (.setLocalDescription local desc)
      ; (.setRemoteDescription remote desc)
      (.createAnswer remote #(dispatch [:got-remote-description %]) #())
      (assoc-in db [:local :description] (.-sdp desc)))))

(reg-event-db
 :create-offer
 (fn [db _]
   (let [
         local (get-in db [:local :connection])
         remote (get-in db [:remote :connection])
         success-fn #(dispatch [:got-local-description %])
         failure-fn #(dispatch [:got "failed to create offer:" %])]
      (.createOffer local success-fn failure-fn)
      db)))

(reg-event-db
  :create-channel
  (fn [db _]
    (let [local (get-in db [:local :connection])
          remote (get-in db [:remote :connection])
          send-channel (.createDataChannel local "send-data-channel" #js {:ordered true})]
      (set! (.-onopen send-channel) #(dispatch [:got "send channel opened:" %]))
      (set! (.-onclose send-channel) #(dispatch [:got "send channel closed:" %]))
      (set! (.-onerror send-channel) #(dispatch [:got "send channel error:" %]))
      (set! (.-onmessage send-channel) #(dispatch [:got "send channel message:" %]))
      (set! (.-ondatachannel remote) #(dispatch [:got "got remote channel" %]))
      (dispatch [:got "new send channel:" send-channel])
      (dispatch [:got "send channel state:" (.-readyState send-channel)])
      (dispatch [:create-offer])
      (assoc-in db [:local :channel] send-channel))))

(reg-event-db
  :call-peer
  (fn [db _]
    (let [cfg {"iceServers" [{"urls" "stun:stun.l.google.com:19302"}]}
          local-conn (js/RTCPeerConnection. (clj->js cfg))
          remote-conn (js/RTCPeerConnection. (clj->js cfg))]
      (set! (.-onicecandidate local-conn) #(dispatch [:got "local ice candidate:" %]))
      (set! (.-onicecandidate remote-conn) #(dispatch [:got "remote ice candidate:" %]))
      (dispatch [:create-channel])
      (-> db
        (assoc-in [:local :connection] local-conn)
        (assoc-in [:remote :connection] remote-conn)))))

(defn test-component
  []
  [:div
   [:button {:on-click #(dispatch [:call-peer #_{:optional [{:RtpDataChannels true}]}])}
    "call"]
   [:p "Local Description:"]
   [:p @(subscribe [:local-description])]
   [:p "Remote Description:"]
   [:p @(subscribe [:remote-description])]
   [:button {:on-click #(dispatch [:send-data "hello!"])}
    "say hello"]])

(defcard-rg test
  [test-component])
