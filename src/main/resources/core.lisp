(defmacro defun (name args & body)
  `(def ,name (lambda ,args ,@body)))

(defmacro if (pred then else)
  `(cond
    (,pred ,then)
    (t ,else)))

(defmacro when (pred & body)
  `(cond
    (,pred (do ,@body))))

(defun cadr (lst) (car (cdr lst)))
(defun caar (lst) (car (car lst)))
(defun cddr (lst) (cdr (cdr lst)))

(defun caddr (lst) (car (cdr (cdr lst))))
(defun caadr (lst) (car (car (cdr lst))))
(defun cadar (lst) (car (cdr (car lst))))

(defun map (f x)
  (cond
   (x (cons (f (car x)) (map f (cdr x))))
   (t nil)))

(defmacro let (bindings & body)
  `((lambda ,(map car bindings)
      ,@body)
    ,@(map cadr bindings)))

(defun -let-helper (bindings body)
  (if bindings
      `(((lambda (,(caar bindings))
            ,@(-let-helper (cdr bindings) body))
          ,(cadar bindings)))
    body))

(defmacro let* (bindings & body)
  (car (-let-helper bindings body)))
