<?php

//This script is designed by Android-Examples.com
//Define your host here.
$hostname = "localhost";
//Define your database username here.
$username = "root";
//Define your database password here.
$password = "";
//Define your database name here.
$dbname = "data";
 
 $con = mysqli_connect($hostname,$username,$password,$dbname);
 
 $name = $_POST['name'];
 $email = $_POST['email'];
  
 $Sql_Query = "insert into googledata (name,email) values ('$name','$email')";
 
 if(mysqli_query($con,$Sql_Query)){
	echo 'Data Inserted Successfully';
 }
 else{
	echo 'Try Again';
 }
 mysqli_close($con);
?>