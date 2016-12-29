<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    import = "com.SearchDriver"
    import = "com.SearchIndex"
    import ="java.util.*"
    %>
    
 <%! int p=-1; %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="input-style.css">
<style type="text/css">
plink{ color: #2079df; font-size: 12px; position:absolute;left:200px;}
</style>
<title>Search Results</title>
</head>
<body>

		<form name="input" action="searchjsp.jsp" method="get">
			<h2>CS242 Search Engine</h2>
		        <div class="col-3 input-effect">
		        	<input class="effect-16" type="text"  placeholder="" name="key">
		            <label>Please Input Keyword</label>
		            <input type="hidden" value="0" name="start">
		            <span class="focus-border"></span>
		        </div>
			
			
			<!-- <input type="submit" value="Submit"> -->
		</form>
		
		<a style="color: #333; font-size: 12px;position:relative; left:90px;" href=<%="http://localhost:8080/SearchEngine/searchjsp.jsp?key="+request.getParameter("key")+"&start="+(Integer.parseInt(request.getParameter("start"))-10)%>>Privious Page</a>
		<a style="color: #333; font-size: 12px;position:relative; left:180px;" href=<%="http://localhost:8080/SearchEngine/searchjsp.jsp?key="+request.getParameter("key")+"&start="+(Integer.parseInt(request.getParameter("start"))+10)%>>Next Page</a>
		
		<%
		
		if(Integer.parseInt(request.getParameter("start"))<0||Integer.parseInt(request.getParameter("start"))>SearchIndex.getResultNum()){
			out.println("<h3>No Results in this page!</h3>");
		}else{
			List r = SearchDriver.search(request.getParameter("key"),Integer.parseInt(request.getParameter("start")));
			out.println("<h3>Search:"+request.getParameter("key")+","+SearchIndex.getResultNum()+"results found.</h3>");
			for(int i=0;i<r.size();i++){
				out.println("<h3>"+r.get(i)+"</h3>");
			}
		}
		
		
		%>
		

</body>
</html>