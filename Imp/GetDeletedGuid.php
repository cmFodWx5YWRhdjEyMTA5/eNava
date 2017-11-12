<?php
// read entire file in to stsring
 $json = file_get_contents('php://input');
 
 require_once('config.php');
 
 // connect to database
$db=mysql_connect('localhost','root','');

if(!$db) 
{
    die('Could not connect: '.mysql_error());
}
$connection_string=mysql_select_db('web1abmr_db',$db);
 if(isset($json) && !empty($json))
{

// remove all slashess fromjson string
    $user_post_data = stripslashes($json);
 
 // decode json data in to array
$user_decode_data = json_decode($user_post_data,true);
    // now decode json data to separate post content
	
	// remove root array 
$user_decode_data1 =	array_shift($user_decode_data);
  //web1abmr_db
 
 // create instance of array for store results in array
$results_array = array();


 
 // foreach loop
 foreach($user_decode_data1 as $obj )
 
 {
  
  // for check post is delete or not
          $existquery ="SELECT count(*) as total FROM wp_posts WHERE guid = '".$obj."' ";
		
		$existsresult = mysql_query($existquery);
		
		if($existsresult)
		{
	     	while($row = mysql_fetch_assoc($existsresult))
          {
             //$results_array[] = $obj;
		  
		     $temp = $row['total'];
		     //echo $temp;
			   // if count is equal to 0
			 if($temp == 0)
              {
			    $results_array[] = $obj;
			  }
			  // if count is equal to 1
 			  else if($temp == 1)
			  {
			  // not deleted and not equal to publish
			  	  $status = 'publish';
		          $query ="SELECT count(*) as subtotal FROM wp_posts WHERE guid = '".$obj."' AND post_status != '".$status."' ";
				  $result = mysql_query($query);
	                   //echo $result;
	                       // if result is true
                      if($result) 
                         { 
                            while($row = mysql_fetch_assoc($result))
                              {
                                 $tempv = $row['subtotal'];
								 
								 if($tempv == 1)
								   {
								    $results_array[] = $obj;
								   }
                              }
  
                          }
			   
			  }
			  
			  
				
		
		  
        }
	   }
	  
		
	}

// return json format
   header('Content-type: application/json');
   
   // encode array to json
    $json_data = json_encode($results_array);
		
	  echo $json_data;
	   

 }
 
 
 

 else
    {
        $error="empty array";
        echo $error;
}   
@mysql_close($conn);

?>

    
   
 