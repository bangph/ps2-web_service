#/usr/bin/python2.4
#
#

import psycopg2

# Try to connect
try:
	conn=psycopg2.connect("dbname='ps2' user='postgres' password='postgres'")
	cur = conn.cursor()
	try:
	    cur.execute("SELECT coverageid, ST_AsText(shape) from dataset" )
	except:
	    print "I can't SELECT from bar"

	rows = cur.fetchall()
	print "\nRows: \n"
	i = 0;
	for row in rows:
		print "Row i: " + str(i + 1)
		coverageID = row[0]
		shapeText = row[1]
		firstIndex = shapeText.rfind("(");
		lastIndex = shapeText.find(")");
		
		print shapeText	    
		#print shapeText[firstIndex + 1:lastIndex].split(",")
		print "\n"
		listString = shapeText[firstIndex + 1:lastIndex].split(",")
		
		length = len(listString)
		listString.pop(length - 2)
		
		# join the list of coordinates after remove the second most error
		updateText = "POLYGON((" + ','.join(listString) + "))";
		
		# update shape with the stripped wrong value from shapeText
		cur.execute("Update dataset set shape=st_geomfromtext('" + updateText + "') where coverageID='" + coverageID + "'");
		conn.commit();
		i += 1;
		
except Exception, e:
	    print "I am unable to connect to the database." + str(e)


