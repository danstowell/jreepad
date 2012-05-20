<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Vote on the choice of logo...</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<style type="text/css">
body {
margin: 10px;
border: 5px ridge green;
padding: 10px;
text-align: center;
}
</style>
</head>

<body>

<?php

//$dbcon = mysql_connect('mysql.sourceforge.net', 'jreepad', 'party99');
//mysql_select_db('jreepad', $dbcon);
$dbcon = mysql_connect('mysql4-j.sourceforge.net', 'j96093rw', 'party99');
mysql_select_db('j96093_jreepad', $dbcon);

echo mysql_error();

$ip = mysql_real_escape_string($REMOTE_ADDR);
$vote = mysql_real_escape_string($_REQUEST['vote']);

if(strlen($vote)==0)
{
   die("<p>No option detected! This might be a mistake. I hope not, though.</p>");
}

mysql_query("DELETE FROM LOGOVOTES WHERE ip='$ip'", $dbcon);
mysql_query("INSERT INTO LOGOVOTES SET ip='$ip', vote='$vote'", $dbcon);

echo mysql_error();

?>
<p>Thanks - your vote has been recorded!</p>
</body>
</html>