UPDATE assessment
SET feedback_due_date = due_date + INTERVAL '4 weeks'
WHERE feedback_due_date IS NULL;