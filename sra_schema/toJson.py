#!/usr/bin/env python
import pandas as pd
import argparse
import sys

parser = argparse.ArgumentParser()
parser.add_argument("--path", metavar="<PATH>", dest="path", help="enter the path to the metadata table (xlsx, csv, tsv). By default 'aln_F4.bam' will be used",
                    type=str, default="metadataSRA.xlsx")
parser.add_argument("--outname", metavar="<OUT>", dest="out", help="name for output .json file",
                    type=str, default="sra")
parser.add_argument("--sheetname", metavar="<SNAME>", dest="sname", help="enter the sheet's name, if the input is excel and contains more than one sheet",
                    type=str, default="SRA_MIxS_mandatory")
parser.add_argument("--tabletype", metavar="<TAB>", dest="tab", help="enter the format of the input sheet: xls, csv or tsv",
                    type=str, default="xls")
parser.add_argument("--type", metavar="<TYPE>", dest="type", help="enter the column index name storing json varialbe type.",
                    type=str, default= "json type")
parser.add_argument("--openbis", metavar="<obis>", dest="obis", help="enter the column index name storing the OpenBIS variable type.",
                    type=str, default= "OpenBIS type")
parser.add_argument("--label", metavar="<LABEL>", dest="label", help="enter the column index name storing the item name",
                    type=str, default="Item")
parser.add_argument("--description", metavar="<DESCR>", dest="descr", help="enter the column index name storing the item's description",
                    type=str, default="Definition")
parser.add_argument("--enum", metavar="<ENUM>", dest="enum", help="enter the column index name storing a string of restricted vocabulary in the format of [str1|str1|...]",
                    type=str, default='Value syntax')
parser.add_argument("--property", metavar="<PROP>", dest="prop", help="enter the column index name storing the property name",
                    type=str, default='Structured comment name')
parser.add_argument("--mand", metavar="<MAND>", dest="mand", help="enter the string or symbol used to indicate that a property is mandatory for submission",
                    type=str, default='M')
parser.add_argument("--sheetdict", metavar="<DICT>", dest="dict", help="in case of different data submission sheets (as in MIxS), enter a dictionary mapping the column name to a description",
                    type=dict, default={'migs_eu':'mimimum information on genome sequence: eukaryotes',
                                        'migs_ba':'mimimum information on genome sequence: bacteria',
                                        'migs_pl':'mimimum information on genome sequence: plants',
                                        'migs_vi':'mimimum information on genome sequence: virus',
                                        'migs_org':'mimimum information on genome sequence: organella',
                                        'me':'mimimum information on metagenome sequence',
                                        'mimarks_s':'mimimum information on marker gene sequence: survey',
                                        'mimarks_c':'mimimum information on marker gene sequence: speciment',
                                        'misag':'mimimum information on single amplified genome',
                                        'mimag':'mimimum information on metagenome-assembled genome',
                                        'miuvig':'mimimum information on uncultivated virus genome'})

# print help message for user
parser.print_help()

# get command line arguments
args = parser.parse_args()

# read files from path
path = args.path
sname = args.sname
tab = args.tab
property = args.prop
type = args.type
openbis = args.obis
label = args.label
descr = args.descr
enum = args.enum
sheetDict = args.dict
mandatory = args.mand
out = args.out

#read table into pd.DataFrame
if (tab=='xls'):
    in_df = pd.read_excel(path, sname).fillna('-')
elif(tab=='csv'):
    in_df = pd.read_csv(path, sep=',')
elif(tab=='tsv'):
    in_df = pd.read_csv(path, sep='\t')
else:
    print('ERROR: entered table format is not supported, please choose "xls", "csv" or "tsv"')

#list column names if there are several types of submission sheets
cols = [key for key in sheetDict]

#*****FUNCTIONS*****

#get items (type, @comment, label, description) for each property entry
def getItems(df, r):
    items= ('type', 'openbis_type', 'label', 'description', 'enum')
    ref=(type, openbis, label, descr, enum)
    #pair items with table entries
    #for i in range(0, len(items)-1):
    #    print('\t'+'\t'+'\t'+'\t'+'\t'+'"'+items[i]+'":"'+df.iloc[r].loc[ref[i]]+'",')
    #check it there is a controlled vocabulary in format [name1|name2|etc]
    if '[' in df.iloc[r].loc[enum][0]:
        for i in range(0, len(items)-1):
            print('\t'+'\t'+'\t'+'\t'+'\t'+'"'+items[i]+'":"'+df.iloc[r].loc[ref[i]]+'",')
        s=df.iloc[r].loc[enum][1:-1].split('|')
        #string of vocabulary with double quotes
        dq = ",".join(['"{0}"'.format(x) for x in s])
        print('\t'+'\t'+'\t'+'\t'+'\t'+'"enum":[{}]'.format(dq))
    else:
        for i in range(0, len(items)-2):
            print('\t'+'\t'+'\t'+'\t'+'\t'+'"'+items[i]+'":"'+df.iloc[r].loc[ref[i]]+'",')
        print('\t'+'\t'+'\t'+'\t'+'\t'+'"'+items[-2]+'":"'+df.iloc[r].loc[ref[-2]]+'"')
        #print('\t'+'\t'+'\t'+'\t'+'\t'+'"enum":[]')

#get properties and join them with their respective items
def getProperties(df):
    for i in range(0, df.shape[0]):
        print('\t'+'\t'+'\t'+'\t'+'"Q_'+df.iloc[i].loc[property].upper()+'":{')
        getItems(df, i)
        if (i<df.shape[0]-1):
            print('\t'+'\t'+'\t'+'\t'+'\t'+'},')
        else:
            print('\t'+'\t'+'\t'+'\t'+'\t'+'}')

#define the list of required arguments
def lstM(df, sheet):
    mlst = df[df[sheet]==mandatory].loc[:,property].to_list()
    mlst = ['Q_'+ s.upper() for s in mlst]
    dq = ",".join(['"{0}"'.format(x) for x in mlst])
    return dq

#print one property sheet for SRA (e.g. migs_eu, migs_ba, etc)
def printSheet(sheet):
    print('\t'+'\t'+'"Q_'+sheet.upper()+'":{')
    print('\t'+'\t'+'\t'+'"description":"'+sheetDict[sheet]+'",')
    print('\t'+'\t'+'\t'+'"properties":{')
    getProperties(in_df)
    print('\t'+'\t'+'\t'+'},')
    print('\t'+'\t'+'\t'+'"required":[{}]'.format(lstM(in_df, sheet)))
    #check if it is last sheet to print
    if(sheet==cols[-1] ):
        print('\t'+'\t'+'}')
    else:
        print('\t'+'\t'+'},')

#parse input table and print out.json
import sys
with open(out+'.json', 'w') as f:
    sys.stdout = f 
    print('{')
    print('\t'+'"definitions":{')
    for c in cols:
        printSheet(c)
    print('\t'+'}')
    print('}')

    f.close()
    # Reset the standard output
#sys.stdout = original_stdout 