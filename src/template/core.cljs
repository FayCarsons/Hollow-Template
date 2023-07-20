(ns template.core
  (:require [hollow.util :as u]
            [hollow.webgl.core :refer [start-hollow!
                                       hollow-context]]
            [hollow.dom.canvas :refer [maximize-gl-canvas
                                       canvas-resolution]]
            [hollow.webgl.shaders :refer [run-purefrag-shader!]]
            [kudzu.tools :refer [unquotable]]
            [kudzu.core :refer [kudzu->glsl]]))

(def kudzu-wrapper 
  (partial kudzu->glsl
           '{:precision {float highp
                         int highp
                         sampler2D highp
                         usampler2D highp}}))
(def render-frag
  (unquotable
   (kudzu-wrapper
    '{:outputs {fragColor vec4}
      :uniforms {size vec2}
      :main ((=vec2 pos (/ gl_FragCoord.xy size))
             
             (= fragColor (vec4 pos 0 1)))})))

(defn render! [{:keys [gl resolution] :as state}]
  (run-purefrag-shader! gl
                        render-frag
                        resolution
                        {"size" resolution})
  state)

(defn update-sketch! [{:keys [gl] :as state}]
  (maximize-gl-canvas gl)
  (-> state
      (assoc :resolution (canvas-resolution gl))
      render!))

(defn init-sketch! [gl]
  {:gl gl})

(defn init []
  (start-hollow! init-sketch! update-sketch!))

(defn ^:dev/after-load restart! []
  (js/document.body.removeChild (.-canvas (hollow-context)))
  (init))

(defn pre-init []
  (js/window.addEventListener "load" (fn [_] (init))))