; Example map function.
(def map
     (lambda (f x)
       (cond
        x (cons (f (car x)) (map f (cdr x)))
        t nil)))

; Double
(def double (lambda (n) (* n 2)))

; For testing comment
(prn "Double: " (map double '(0 1 2 3 4 5 6)))
