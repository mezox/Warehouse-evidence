#! usr/env/python

import re
from urllib2 import *

num = 0

while True:

	num += 1
	if num == 3:
		break

	try:
		response = urlopen("http://notebooky.heureka.sk/?f=" + str(num))
	except URLError, e:
		print "error"

	mainpage = response.read()

	links = re.findall('class="product-container"><h2><a href="(.+?)">(.+?)<', mainpage)
	for link in links:
		print link[1]
		i = 0

		try:
			response1 = urlopen(link[0])
		except URLError, e:
			print "error"

		gal = re.search('href="(.+?/galerie/)', response1.read())
		if gal:
			try:
				response2 = urlopen(gal.group(1))
			except URLError, e:
				print "error"

			jpg = re.findall('(..obrazek=.+?)">', response2.read())
			for j in jpg:
				try:
					response3 = urlopen(gal.group(1) + j)
				except URLError, e:
					print "error"		

				page = response3.read()
				p = re.search('main-image"><img src="(.+?)"', page)
				if p:

					i += 1
					if i > 4:
						print 'i>4'
						continue

					print p.group(1)				
					f = open('jpg/' + link[1].replace('/', ' ') + '__' + str(i) + '.jpg', 'wb')
					f.write(urlopen(p.group(1)).read())
					f.close()

					


