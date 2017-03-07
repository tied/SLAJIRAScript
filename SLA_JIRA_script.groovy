import com.atlassian.core.util.DateUtils
import com.atlassian.jira.ComponentManager
import com.atlassian.jira.issue.history.ChangeItemBean
import com.atlassian.jira.issue.Issue

import com.atlassian.jira.util.ErrorCollection

import com.onresolve.jira.groovy.canned.CannedScript
import com.onresolve.jira.groovy.canned.utils.CannedScriptUtils

import org.apache.log4j.Category
import com.atlassian.jira.issue.link.IssueLinkManager

import com.onresolve.jira.groovy.canned.utils.WorkflowUtils

import com.atlassian.jira.issue.link.IssueLink
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.crowd.embedded.api.User


ComponentManager componentManager = ComponentManager.getInstance()
def changeHistoryManager = componentManager.getChangeHistoryManager() 


// ¬ысчитываем врем€ потраченное в эскалированных тикетах и заносим в переменную destOverallM
    def projectManager = componentManager.getProjectManager()
    def String FIELD_LINK_TYPE = 'escalates'
  def html
  def destOverallM = 0
    String linkTypeId = "10300"
    String linkDirection = "outward"

        IssueLinkManager linkMgr = ComponentManager.getInstance().getIssueLinkManager()

        List links = []
if (linkDirection == CannedScriptUtils.OUTWARD_FIELD_NAME) {
  links = linkMgr.getOutwardLinks(issue.id)
}
else if (linkDirection == CannedScriptUtils.INWARD_FIELD_NAME) {
  links = linkMgr.getInwardLinks(issue.id)
}

links.each {IssueLink link ->
  if (link.getLinkTypeId() == linkTypeId as Long) {
    MutableIssue destIssue = linkDirection == CannedScriptUtils.OUTWARD_FIELD_NAME ? link.getDestinationObject() : link.getSourceObject()
    if (destIssue.getStatus().name.toString() == "Resolved") {
      destOverallM = destOverallM + destIssue.getResolutionDate().getTime() - destIssue.getCreated().getTime()
    }
    else {
      destOverallM = destOverallM + System.currentTimeMillis() - destIssue.getCreated().getTime()
    }
  }
}
    
def inLocalizationName = "Localization"
def inOpenName = "Open"
def inEscalatedName = "Escalated"
def inWaitingforName = "Waiting for"
def inReopenName = "Reopened"
def inEscalatedOSMPName = "Escalated to OSMP";
def inInProgressName = "In Progress"

def lt = [0]
def ot = [0]
def et = [0]
def wt = [0]
def re = [0]
def eot = [0]
def it = [0]

def inOverallM
def add = ""
def inOpenM = System.currentTimeMillis() - issue.getCreated().getTime()
ot << inOpenM

// ¬ычисл€ем врем€ между переходом из одного статуса в другой и заполн€ем одномерные массивы

changeHistoryManager.getChangeItemsForField (issue, "status").reverse().each {ChangeItemBean item ->
  
  def timeDiff = System.currentTimeMillis() - item.created.getTime()
  if (item.fromString == inOpenName){
    ot << -timeDiff
  }
  if (item.toString == inOpenName){
    ot << timeDiff
  }
  if (item.toString == "Resolved"){
    resolved = item.created.getTime()
  }
  if (item.fromString == inLocalizationName) {
    lt << -timeDiff
  }
  if (item.toString == inLocalizationName){
    lt << timeDiff
  }
  if (item.fromString == inEscalatedName) {
    et << -timeDiff
  }
  if (item.toString == inEscalatedName){
    et << timeDiff
  }
  if (item.fromString == inWaitingforName) {
    wt << -timeDiff
  } // end of if
  if (item.toString == inWaitingforName) {
    wt << timeDiff
  } // end of if
  if (item.fromString == inReopenName) {
    re << -timeDiff
  } // end of if
  if (item.toString == inReopenName) {
    re << timeDiff
  } // end of if
  if (item.fromString == inEscalatedOSMPName) {
    eot << -timeDiff
  } // end of if
  if (item.toString == inEscalatedOSMPName) {
    eot << timeDiff
  } // end of if
  if (item.fromString == inInProgressName) {
    it << -timeDiff
  } // end of if
  if (item.toString == inInProgressName) {
    it << timeDiff
  } // end of if
  // end of if
}

/*
def typeT = [0]
changeHistoryManager.getChangeItemsForField (issue, "IssueType").reverse().each {ChangeItemBean tt ->
  
  if  (tt.toString== "Problem" || tt.fromString == "Problem") {
    typeT=1
  }
  else {  typeT=2
  }   
  log.warn("typeT:"+typeT)
}

*/

// ћассив сла

int[][] matrixA;
matrixA = new int[4][4];

matrixA[0][0] = 24
matrixA[0][1] = 12
matrixA[0][2] = 4
matrixA[0][3] = 2

matrixA[1][0] = 720
matrixA[1][1] = 480
matrixA[1][2] = 240
matrixA[1][3] = 120

matrixA[2][0] = 720
matrixA[2][1] = 480
matrixA[2][2] = 120
matrixA[2][3] = 1

matrixA[3][0] = 4320
matrixA[3][1] = 4320
matrixA[3][2] = 4320
matrixA[3][3] = 4320


// ќпредел€ем тип тикета
def Type2
if (issue.getIssueType().name.toString() == "Incident") { 
  Type2 = 0
}
else if  (issue.getIssueType().name.toString() == "Problem") { 
  Type2 = 1 
}
else if  (issue.getIssueType().name.toString() == "Request") {
  Type2 = 2
}
else {  Type2 = 3
}
 // end of if-else
  
// ќпредел€ем приоритет тикета и выбераем сла
  
  def Priority
  //def ttt
if (issue.getPriorityObject().name.toString() == "Minor") { 
  Priority = 0
  ttt = matrixA[Type2][0]    
}
else if (issue.getPriorityObject().name.toString() == "Major") {  
  Priority = 1 
  ttt = matrixA[Type2][1]    
} 
else if (issue.getPriorityObject().name.toString() == "Critical") {    
  Priority = 2  
  ttt = matrixA[Type2][2]  
}  
  else 
{
  Priority = 3  
  ttt = matrixA[Type2][3]  
} // end of if-else
  
  
// ≈сли решена, то подсчитываем врем€ между получением статуса resolved и временем создани€ тикета
// если нет, то текущее врем€ - врем€ создани€ тикета    
if (issue.getStatus().name.toString() == "Resolved") {
  inOverallM = resolved - issue.getCreated().getTime()
}
else {
  inOverallM = System.currentTimeMillis() - issue.getCreated().getTime()
}
  
// ѕереводим из мс в секунды и берем модуль от суммы €чеек в каждом массиве
  def inOpenS = Math.round(Math.abs(ot.sum()) / 1000)
  def inLocalizationS = Math.round(Math.abs(lt.sum()) / 1000)
  def inEscalationS;
  def inReopenS = Math.round(Math.abs(re.sum()) / 1000)
  def inEscalationOSMPS = Math.round(Math.abs(eot.sum()) / 1000)
  def inInProgressS = Math.round(Math.abs(it.sum()) / 1000)
  def inWaitingForS = Math.round(Math.abs(wt.sum()) / 1000);
   
  // ≈сли ескалированный тикет был закрым 
if (destOverallM > 0) {
  // ≈сли врем€ эскалации больше destOverallM, то высчитываем разницу между временем эскалации и destOverallM
  // и заносим в inEscalationS
  if (Math.abs(et.sum()) > destOverallM) {
    inEscalationS = Math.round((Math.abs(et.sum()) - destOverallM) / 1000)
  }
  // ≈сли нет то заносим врем€ эскалации в inEscalationS и добавл€ем в в переменную add вывода на страницу тикета 
  // информационное сообщение
  else {
    inEscalationS = Math.round(Math.abs(et.sum()) / 1000)
    add = add + "Work was carried out in different issues at the same time\n"
  }
}
// ≈сли нет то заносим врем€ эскалации в inEscalationS   
else {
  inEscalationS = Math.round(Math.abs(et.sum()) / 1000)
}


 // переводим из секунд в формат времени чч.мм.сс
  def inOpen = DateUtils.getDurationString(inOpenS)
  def inLocalization = DateUtils.getDurationString(inLocalizationS)
  def inEscalation = DateUtils.getDurationString(inEscalationS)
  def inOverall = DateUtils.getDurationString(Math.round(inOverallM / 1000)) // округление до ближайшего целого
  
  
  
  // ѕодсчет времени на выполнение задачи как суммы эскалации, локализации и и старта
  def inOverall1S = inOpenS + inLocalizationS + inEscalationS
  def inOverall1 = DateUtils.getDurationString(inOverall1S) + " " + inOverall1S % 60 +"s"   // делим на 60 (перевод в минуты) и возвращаем остаток
  // ѕодсчет времени на выполнение задачи как суммы всех действий
  def inOverallF = inOpenS + inLocalizationS + inEscalationS + inReopenS + inEscalationOSMPS + inInProgressS + inWaitingForS 
  def inOverall2 = DateUtils.getDurationString(inOverallF) + " " + inOverallF % 60 +"s"
  def inOverallW = inOverallF - inWaitingForS
  
  
if(inOpenS > 0) {
  if (Math.abs(ot.sum()) < 1*60*60*1000 ) {
    html = "<font color=\"black\"><b>SLA: Responce time: </b>" + inOpen + " "+ inOpenS % 60 +"s</font><br>"
  }
  else {
    html = "<font color=\"black\"><b>SLA FAIL: Responce time: </b>" + inOpen + " "+ Math.round(Math.abs(ot.sum()) / 1000) % 60 +"s</font><br>"
  }
}
  /*
  if(inLocalizationS > 0) {
  if (Math.abs(pt.sum()) < 2*60*60*1000) {
  html = html + "<font color=\"green\"><b>[SLA OK]</b> Localization: " + inLocalization + " "+ inLocalizationS % 60 +"s</font><br>"
  }
  else {
  html = html + "<font color=\"red\"><b>[SLA FAILED]</b> Localization: " + inLocalization + " "+ inLocalizationS % 60 +"s</font><br>"
  }
  }
  
  if(inEscalationS > 0) {
  if (Math.abs(et.sum()) - destOverallM < 4*60*60*1000) {
  html = html + "<font color=\"green\"><b>[SLA OK]</b> Escalation: " + inEscalation + " "+ inEscalationS % 60 +"s</font><br>"
  }
  else {
  html = html + "<font color=\"red\"><b>[SLA FAILED]</b> Escalation: " + inEscalation + " "+ inEscalationS % 60 +"s</font><br>"
  }
  }
  */
  
  
  /* 
  if(typeT==1) {
  html = html + "<font color=\"green\"><b>[Type]</b> Incidents</font><br>";
  }
  else if (typeT==2){
  html = html + "<font color=\"red\"><b>[Type]</b> Other</font><br>"
  }
  
  if(Type2==1) {
  html = html + "<font color=\"green\"><b>[Type]</b> Incident</font><br>";
  }
  else if (Type2==2){
  html = html + "<font color=\"red\"><b>[Type]</b> Other</font><br>"
  }
  */ 
  
if (Type2==0) {
  if (issue.getStatus().name.toString() == "Resolved") {
    if (inOverallF < ttt*60*60 ) {
      html = html + "<font color=\"black\"><b>SLA: Solution of incident: </b>"+ inOverall2 +"</font><br>"
    }
    else {
      html = html + "<font color=\"black\"><b>SLA FAIL: Solution of incident: </b>"+ inOverall2 +"</font><br>"
    }
    
    
  }
} // end of if
  
else if (Type2==1){
  if (issue.getStatus().name.toString() == "Resolved") {
    if (inOverallF < ttt*60*60 ) {  
      html = html + "<font color=\"black\"><b>SLA: Permanent solution: </b>"+ inOverall2 +"</font><br>"
    }
    else {
      html = html + "<font color=\"black\"><b>SLA FAIL: Permanent solution: </b>"+ inOverall2 +"</font><br>"
    }
    
  } 
}
else if (Type2==2){
  if (issue.getStatus().name.toString() == "Resolved") {
    if (inOverallF < ttt*60*60 ) {  
      html = html + "<font color=\"black\"><b>SLA: Request Completed in: </b>"+ inOverall2 +"</font><br>"
    }
    else {
      html = html + "<font color=\"black\"><b>SLA FAIL: Request Completed in: </b>"+ inOverall2 +"</font><br>"
    }
    
  }
  
}

else {
  if (issue.getStatus().name.toString() == "Resolved") {
    if (inOverallF < ttt*60*60 ) {  
      html = html + "<font color=\"black\"><b>SLA: It has been closed after: </b>"+ inOverall2 +"</font><br>"
    }
    else {
      html = html + "<font color=\"black\"><b>SLA FAIL: It has been closed after: </b>"+ inOverall2 +"</font><br>"
    }
    
  }
  
}


/*
  html = html + "inOverallF " + inOverallF +"<br>"
  html = html + "ttt " + ttt*60*60  +"<br>"
  html = html + "ttt " + ttt +"<br>"
  html = html + "ot " + ot.sum()/60/1000 +"<br>"
  html = html + "lt " + lt.sum() / 60 / 1000 +"<br>"
  html = html + "et " + et.sum() / 60 / 1000  +"<br>"
  html = html + "wt " + wt.sum() / 60 /1000  +"<br>";
  html = html + "re " + re.sum() / 60 /1000  +"<br>";
  html = html + "it " + it.sum() / 60 /1000  +"<br>";
  html = html + "eot " + eot.sum() / 60 /1000  +"<br>"
  html = html + "inLocalization " + inLocalization +"<br>";
  html = html + "inEscalation " + inEscalation +"<br>";
  html = html + "inReopen " + DateUtils.getDurationString(inReopenS) +"<br>";
  html = html + "inEscalationOSMP " + DateUtils.getDurationString(inEscalationOSMPS) +"<br>";
  html = html + "inInProgress " + DateUtils.getDurationString(inInProgressS) +"<br>";
  html = html + "inWaitingFor " + DateUtils.getDurationString(inWaitingForS) +"<br>"
  html = html + "inOverall " + inOverall +"<br>";
  html = html + "inOverall all " + inOverall2 +"<br>"
 */  

 add = add + "Waiting for : " + DateUtils.getDurationString(inWaitingForS)  + " " + inWaitingForS % 60 +"s" +"\n" 
 add = add + "SLA - Waiting for : " + DateUtils.getDurationString(inOverallW)  + " " + inOverallW % 60 +"s" +"\n" 
 html = "<abbr title=\"" +add+ "\" style=\"cursor: help;\">" + html + "</abbr>"
 /*
  add = add + "Overall in 1st line: "+ inOverall1 +"\n"
  
  if (destOverallM > 0) {
  add = add + "Overall in other lines: "+ DateUtils.getDurationString(Math.round(destOverallM / 1000)) +"\n"
      add = add + "Overall in other lines: "+ DateUtils.getDurationString(Math.round(destOverallM / 1000)) +"\n"
}
  add = add + "Total time: " + inOverall
  html = "<abbr title=\"" +add+ "\" style=\"cursor: help;\">" + html + "</abbr>"
  */
return html