cd E:\开发必备\Genetic Programming\ECJ\jar
java -jar EvaluationGP.jar -from app/JSP/MultipleEvaluation_TSIDOCBA.params -p pop.file=$tmp/job.%1.allRules.log -p stat.file=/log/job.%1.Result_Evaluation.stat -p stat.child.0.file = /log/job.%1.Details_Evaluation.stat
exit