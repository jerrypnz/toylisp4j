(defmacro defun (name args & body)
  `(def ,name (lambda ,args ,@body)))

(defmacro if (pred then else)
  `(cond
    (,pred ,then)
    (t ,else)))

(defun cadr (lst) (car (cdr lst)))
(defun caar (lst) (car (car lst)))
(defun cddr (lst) (cdr (cdr lst)))

(defun caddr (lst) (car (cdr (cdr lst))))
(defun caadr (lst) (car (car (cdr lst))))
(defun cadar (lst) (car (cdr (car lst))))

(defmacro let (bindings & body)
  `((lambda ,(map car bindings)
      ,@body)
    ,@(map cadr bindings)))

(defun map (f x)
  (cond
   (x (cons (f (car x)) (map f (cdr x))))
   (t nil)))
