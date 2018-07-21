#lang plai-typed
; https://pubweb.eng.utah.edu/~cs5510/hw1.html


  (define-type Tree
    [leaf (val : number)]
    [node (val : number)
          (left : Tree)
          (right : Tree)])


; Problem 1
; Sum all the lements of a tree

(define (sum-tree [tree : Tree]) : number
  (type-case Tree tree
    [leaf (value) value]
    [node (value left right) (+ value (+ (sum-tree left) (sum-tree right)))]
    ))

(test (sum-tree (node 5 (leaf 6) (leaf 7))) 18)

; Problem 2
(define (negate [tree : Tree]) : Tree
  (type-case Tree tree
    [leaf (value) (leaf (- 0 value))]
    [node (value left right) (node (- 0 value) (negate left) (negate right))]
    ))
(test (negate (node 5 (leaf 6) (leaf 7))) (node -5 (leaf -6) (leaf -7)))

; Problem 3
(define (contains? [tree : Tree] [val : number]) : boolean
  (type-case Tree tree
    [leaf (value) (equal? value val)]
    [node (value left right) (or (contains? left val) (contains? right val) (equal? value val))]))
(test (contains? (node 5 (leaf 6) (leaf 7)) 6) #t)
(test (contains? (node 5 (leaf 6) (leaf 7)) 7) #t)
(test (contains? (node 5 (leaf 6) (leaf 7)) 5) #t)
(test (contains? (node 5 (leaf 6) (leaf 7)) 8) #f)

; Problem 4
; Return true if sum of every node on path is greater than node.
(define (big-leaves-helper [tree : Tree] [total-sum : number]) : boolean
  (type-case Tree tree
    [leaf (value) (> value total-sum)]
    [node (value left right) (and (big-leaves-helper left (+ total-sum value))
                                  (big-leaves-helper right (+ total-sum value))
                                  (> value total-sum))]))

(define (big-leaves? [tree : Tree]) : boolean
  (big-leaves-helper tree 0))

(test (big-leaves? (node 5 (leaf 6) (leaf 7))) #t)
(test (big-leaves? (node 5 (node 2 (leaf 8) (leaf 6)) (leaf 7))) #f)

; Problem 5
