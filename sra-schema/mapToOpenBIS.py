#!/usr/bin/env python
import pandas as pd
import argparse
import sys
import json

parser = argparse.ArgumentParser()
parser.add_argument("--path", metavar="<STR>", dest="path", help="enter the path to the metadata table (xlsx, csv, tsv). By default 'EGA_SRA_Metadata.xlsx' will be used",
                    type=str, default="EGA_SRA_Metadata.xlsx")
parser.add_argument("--outname", metavar="<STR>", dest="out", help="name for output .json file",
                    type=str, default="mapToOpemBIS")
parser.add_argument("--sheetname", metavar="<STR>", dest="sname", help="enter the sheet's name, if the input is excel and contains more than one sheet",
                    type=str, default="EGA_SRA_MATCHED")
parser.add_argument("--tabletype", metavar="<STR>", dest="tab", help="enter the format of the input sheet: xls, csv or tsv",
                    type=str, default="xls")
parser.add_argument("--type", metavar="<STR>", dest="type", help="enter the column index name storing json variable type.",
                    type=str, default= "json type")
parser.add_argument("--obistype", metavar="<STR>", dest="obistype", help="enter the column index name storing the OpenBIS variable type.",
                    type=str, default= "OpenBIS type")
parser.add_argument("--obisobjtype", metavar="<STR>", dest="objtype", help="enter the column index name storing the OpenBIS object type.",
                    type=str, default= "OpenBIS object type")                    
parser.add_argument("--obisproperty", metavar="<STR>", dest="obisprop", help="enter the column index name storing the openBIS property name",
                    type=str, default="Existing property in OpenBIS")
parser.add_argument("--newproperty", metavar="<STR>", dest="newprop", help="enter the column index name storing the openBIS property name",
                    type=str, default="New property in OpenBIS")
parser.add_argument("--description", metavar="<STR>", dest="descr", help="enter the column index name storing the item's description",
                    type=str, default="Definition")
parser.add_argument("--erproperty", metavar="<STR>", dest="erprop", help="enter the column index name storing the endpoint repository's property name",
                    type=str, default='SRA Structured comment name')
#parser.add_argument("--mand", metavar="<MAND>", dest="mand", help="enter the string or symbol used to indicate that a property is mandatory for submission",
                    #type=str, default='M')
#parser.add_argument("--sheetdict", metavar="<DICT>", dest="dict", help="in case of different data submission sheets (as in MIxS), enter a dictionary as a string (with single quotes) mapping the column name (with double quotes) to a description (with double quotes",
                    #type=str, default='{"migs_eu":"mimimum information on genome sequence: eukaryotes", "migs_ba":"mimimum information on genome sequence: bacteria", "migs_pl":"mimimum information on genome sequence: plants", "migs_vi":"mimimum information on genome sequence: virus", "migs_org":"mimimum information on genome sequence: organella", "me":"mimimum information on metagenome sequence", "mimarks_s":"mimimum information on marker gene sequence: survey", "mimarks_c":"mimimum information on marker gene sequence: speciment", "misag":"mimimum information on single amplified genome", "mimag":"mimimum information on metagenome-assembled genome", "miuvig":"mimimum information on uncultivated virus genome", "Plant":"NCBI SRA: use for any plant sample or cell line", "Virus":"NCBI SRA: use for all virus samples not directy associated with disease", "SarsCov":"NCBI SRA: use for SARS-CoV-2 samples that are relevant to public health", "PathogenEnv":"NCBI SRA environmental pathogen metadata sheet", "PathogenCl":"NCBI SRA: use for pathogen samples that are relevant to public health", "Microbe":"NCBI SRA: use for bacteria or other unicellular microbes when it is not appropriate or advantageous to use MixS, Pathogen of Virus packages", "Metagenome":"NCBI SRA: Use for metagenomic and environmental samples when it is not appropriate or advantageous to use MixS packages", "Invertebrate":"NCBI SRA: use for any invertebrate sample", "Human":"NCBI SRA: Only use for human samples or cell lines that have no privacy concerns. for samples isolated from humans use the Pathogen, Microbe or appropriate MixS package", "BetaLactamase":"NCBI SRA: Use for beta-lactamase gene transformants that have sequence and antibiotic resistance data"}'
                    #)

# print help message for user
parser.print_help()

# get command line arguments
args = parser.parse_args()

path = args.path
sname = args.sname
tab = args.tab
erprop = args.erprop
obprop = args.obisprop
newprop = args.newprop
jsontype = args.type
obtype = args.obistype
objecttype = args.objtype
descr = args.descr
#enum = args.enum
out = args.out

#read table into pd.DataFrame
if (tab=='xls'):
    in_df = pd.read_excel(path, sname).fillna('')
elif(tab=='csv'):
    in_df = pd.read_csv(path, sep=',').fillna('')
elif(tab=='tsv'):
    in_df = pd.read_csv(path, sep='\t').fillna('')
else:
    print('ERROR: entered table format is not supported, please choose "xls", "csv" or "tsv"')

#sort in_df by openBis Object Type
in_df.sort_values(objecttype, inplace=True)

#join entries of columns existing and new properties in OpenBIS in a new column
in_df['openBIS property'] = in_df[[obprop, newprop]].agg(' '.join, axis=1) 
openbis = 'openBIS property'

#dictionary of number of properties per object type
numdict = in_df[objecttype].value_counts().to_dict()

#*****FUNCTIONS*****

#get items (type, obtype, obprop, description) for each end repository property entry
def getItems(df, r):
    items= ('type', 'openbis_type', 'openbis_property', 'description')
    ref=(jsontype, obtype, openbis, descr)
    #pair items with table entries
    for i in range(0, len(items)-1):
        print('\t'+'\t'+'\t'+'\t'+'\t'+'"'+items[i]+'":"'+df.iloc[r].loc[ref[i]]+'",')
    print('\t'+'\t'+'\t'+'\t'+'\t'+'"'+items[-1]+'":"'+df.iloc[r].loc[ref[-1]]+'"')
    
    #print('\t'+'\t'+'\t'+'\t'+'\t'+'}')

#map repository properties to OpenBIS properties
def printMap(df, dic):
    x=0
    for obt in list(dic.keys()):
        #print OpenBIS Object type
        print('\t'+'\t'+'"'+obt+'":{')
        print('\t'+'\t'+'\t'+'"properties":{')
        for i in range (0, dic[obt]):
            #print endpoint repository properties of this OpenBIS Object Type
            print('\t'+'\t'+'\t'+'\t'+'"'+df[erprop].iloc[x+i]+'":{')
            #map to items of this property 
            getItems(df, x+i)
            #close brackets of property items, except last one
            if (i < dic[obt]-1):
                print('\t'+'\t'+'\t'+'\t'+'},')
            elif ((i== dic[obt]-1) & (obt == list(dic.keys())[-1])):
                #close brackets of last property of last object type
                print('\t'+'\t'+'\t'+'\t'+'}')
                print('\t'+'\t'+'\t'+'}')
                print('\t'+'\t'+'}')
            else:
                #close brackets of last property of each object
                print('\t'+'\t'+'\t'+'\t'+'}')
                print('\t'+'\t'+'\t'+'},')
        x+=dic[obt]

#parse input table and print out.json
import sys
with open(out+'.json', 'w') as f:
    sys.stdout = f 
    print('{')
    print('\t'+'"definitions":{')
    printMap(in_df, numdict)
    print('\t'+'}')
    print('}')

    f.close()
    # Reset the standard output
#sys.stdout = original_stdout 