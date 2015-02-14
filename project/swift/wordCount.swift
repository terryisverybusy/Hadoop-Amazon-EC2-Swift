type file;

file wc_script <"wordCount.py">;
file m_py <"mergefile.py">;

app (file outfile) wordCount (file infile, file wc_script)
{
    python @wc_script @infile stdout=@outfile;
}

app (file outfile) merge (file[] infiles, file m_py)
{
    m_script @filenames(infiles) stdout=@outfile;
}

//use the mapper
file infile[] <filesys_mapper;pattern="split-*", location="input">;
file wcfile[];

tracef("test\n");
foreach f,i in infile
{

    wcfile[i]=wordCount(f,wc_script);
}

file mfile <"output/result.txt">;

//merge the output file
mfile=merge(wcfile,m_py);
