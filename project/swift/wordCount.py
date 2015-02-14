import sys
import os
import re
from collections import Counter


#count  tthe word 
cnt=Counter()
for fileName in sys.argv[1:]:
    if not os.path.isfile(fileName):
        print >> sys.stderr, 'wordcount error: file name \"%s\" not exist.' % (fileName)
    f=open(fileName, 'r')
    token=filter(None,re.split('\W+', f.read().decode('utf-8'), flags=re.UNICODE))
    cnt.update(token)
    outstr = '\n'.join(['%d\t%s' % (num, word) for word,num in sorted(cnt.items())])
    print outstr.encode('utf-8')
