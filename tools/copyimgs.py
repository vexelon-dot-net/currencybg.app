#!/usr/bin/python
# -*- coding: utf-8 -*-
##########################################################################
# Renames all files in a directory to a match ISO country codes
# NOTE: Script requires Python 3.3
##########################################################################

import os
import glob
import shutil
import collections
from optparse import OptionParser

# XML 2 SQL column mappings
COUNTRY_CODES = {
'australia': 'au',
'bulgaria': 'bg',
'brazil': 'br',
'canada': 'ca',
'switzerland': 'ch',
'china': 'cn',
'czech-republic': 'cz',
'denmark': 'dk',
'estonia': 'ee',
'european-union': 'eu',
'united-kingdom': 'gb',
'hong-kong': 'hk',
'croatia': 'hr',
'hungary': 'hu',
'indonesia': 'id',
'india': 'in',
'iceland': 'is',
'japan': 'jp',
'south-korea': 'kr',
'lithuania': 'lt', 
'latvia': 'lv', 
'mexico': 'mx', 
'malaysia': 'my', 
'norway': 'no', 
'new-zealand': 'nz',
'philippines': 'ph', 
'poland': 'pl', 
'romania': 'ro', 
'russia': 'ru', 
'sweden': 'se', 
'singapore': 'sg', 
'thailand': 'th', 
'turkey': 'tr', 
'united-states': 'us', 
'south-africa': 'za'
}

def transformFiles(sourceDir, destDir):
	print ("Reading files from {}".format(sourceDir))

	os.chdir(sourceDir);
	for f in glob.glob("*.png"):
		fileName = os.path.splitext(f)[0][0:].lower()
		# print (fileName)
		if fileName in COUNTRY_CODES:
			code = COUNTRY_CODES[fileName.lower()]
			#print ("Found country code: {}".format(code))
			src = os.path.join(sourceDir, f)
			dest = os.path.join(destDir, code + '.png')
			print "copying {} to {}".format(f, dest)
			shutil.copyfile(src, dest)


def createParser():
    parser = OptionParser()
    parser.add_option("-d", "--directory", dest="directory", help="Directory where to look for PNG files.", metavar="FILE")
    parser.add_option("-o", "--out", dest="outDir", help="Output directory where to save PNG files.", metavar="FILE")
    return parser

# Main ############

try:
    parser = createParser()
    (options, args) = parser.parse_args()

    if not options.directory:
        parser.error("Source directory parameter missing!")
    if not options.outDir:
    	options.outDir = os.getcwd()
        # parser.error("Output directory parameter missing!")

    #if len(sys.argv) < 2:
    #   raise Exception('Missing XML <file path> command line argument!')

    if not os.path.isdir(options.directory): 
        parser.error("source directory passed does not exist!")

    if not os.path.isdir(options.outDir): 
        parser.error("output directory passed does not exist!")

    transformFiles(options.directory, options.outDir)
except Exception as inst:
    print (inst.args)
