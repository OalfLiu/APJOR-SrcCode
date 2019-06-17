@echo off
cd E:\开发必备\Genetic Programming\ECJ\jar
java -Xmx1024m -jar EvaluationGP.jar -from app/JSP/FinalEvaluation.params -p pop.file=$tmp/job.%1.allRules.log -p stat.file=/log/job.%1.Result_Evaluation.stat -p stat.child.0.file=/log/job.%1.Details_Evaluation.stat -p evaluation.Replications=10
set s=1
set /a s=s+%1
echo %s%
if %s% gtr 30 (exit)
finaltest.bat %s%