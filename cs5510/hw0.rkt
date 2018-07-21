#lang plai-typed
; https://pubweb.eng.utah.edu/~cs5510/hw0.html

; Problem 1
(define (3rd-power [x : number]) : number
  (* x (* x x)))
(test (3rd-power 3) 27)

; Problem 2 define 42nd power
(define (power [x : number] [y : number]) : number
  (cond
    [(equal? y 0) 1]
    [else (* x (power x (- y 1)))]))
(define (42nd-power [x : number]) : number (power x 42))
(test (power 2 3) 8)

; Problme 3
; Makes a word plural

(define (plural [x : string]) : string
  (cond
    [(equal? (string-ref x (- (string-length x) 1)) #\y) (string-append (substring x 0 (- (string-length x) 1)) "ies")]
    [else (string-append x "s")]))
(test (plural "apple") "apples")
(test (plural "kitty") "kitties")

; Problem 4

(define-type Light
    [bulb (watts : number)
          (technology : symbol)]
    [candle (inches : number)])

(define (energy-usage [light : Light]) : number
  (type-case Light light
    [bulb (watts technology) (/ (* 24 watts) 1000)]
    [candle (inches) 0]))

(test (energy-usage (bulb 100.0 'halogen)) 2.4)
(test (energy-usage (candle 10.0)) 0.0)

; Problem 5
(define (use-for-one-hour [light : Light]) : Light
  (type-case Light light
    [bulb (watts technology) light]
    [candle (inches) (candle (- inches 1))]))

(test (use-for-one-hour (candle 10.0)) (candle 9.0))
(test (use-for-one-hour (bulb 100 'halogen)) (bulb 100 'halogen))