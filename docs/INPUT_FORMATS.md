# PAARSNP Input Formats

For the current databases look in the [resources directory](../build/resources)

## Directory Structure

```
/{taxon-id}/
             ar_agents.csv
             ar_snps.csv  
             ar_snps.fa
             resistance_genes.csv
             resistance_genes.fa
```  
`{taxon-id}` takes the form of the NCBI species code, e.g. `1280` for _Staphylococcus aureus_.   

To add a new species create a new directory containing those files. To extend a species modify the appropriate files ([Current Species](../README.md#current-species))
        
## Files 
### Antimicrobial Agents (_ar_agents.csv_)

Specifies the set of antimicrobial agents that will be reported in the results. Three columns:
1. The short name, typically a three letter code.
1. The class or type
1. The full or descriptive name

#### Example ar_agents.csv
```
Name,type,Full name
AMI,Aminoglycosides,Amikacin
TOB,Aminoglycosides,Tobramycin
KAN,Aminoglycosides,Kanamycin
MET,Beta-Lactam,Methicillin
PEN,Beta-lactams,Penicillin
```

### PAARSNP CSV Common Fields

The PAAR and SNPAR input CSV files have several fields in common. These are described here:
* `Gene Name` The unique name for the resistance gene
* `Resistance Group` The name for the group of genes required to give resistance. 
  * If more than one gene is required both are given the same name and all will have the `Gene Effect` of `RESISTANCE`, e.g. ermA_CLI in the example [below](#example-resistance_genes.csv) includes both ermA and ermA_SDS.
  * If there is only one gene in the set, this column can be left empty and the set name will be derived from the gene name.
* `Set Effect` Can have the following values: 
  * `RESISTANT` - Complete set causes resistance
  * `INDUCED` - Complete set causes inducible resistance 
  * `INTERMEDIATE_NOT_ADDITIVE`  - Complete set causes partial/intermediate resistance
  * `INTERMEDIATE_ADDITIVE`. Complete set causes resistance, while a partial set cause partial resistance. 
* `Effect` Can have the following values:
  * `RESISTANCE` - Standard resistance SNP/gene
  * `MODIFIES_SUPPRESSES` - Suppresses expected resistance (e.g. suppressor gene).
  * `MODIFIES_INDUCED` - Makes resistance inducible instead of constitutive.
* `Resistance Profile` - A comma-separated list of antimicrobials using the name from the first column of the ar_agents.csv

### PAAR (_resistance_genes.csv, resistance_genes.fa_)

The PAAR CSV file specifies the resistance genes for a species. The following additional columns to the ones specified above are required:
* `PID Threshold` - The minimum percent identity of the match as reported by BLAST.
* `Coverage Threshold` - The minimum coverage of the reference sequence by the BLAST match.

The FASTA contains the _DNA_ sequence of the resistance gene. The header must match the corresponding `Gene Name` field.

For a simple example that will cover most cases look at [_Neisseria gonorrhoeae_](../build/resources/485/resistance_genes.csv).

#### Example resistance_genes.csv
```
Gene Name,Resistance Group,Set Effect,Effect,Resistance Profile,PID Threshold,Coverage Threshold
ermA,ermA_ERY,RESISTANT,RESISTANCE,ERY,80.0,80.0
ermA,ermA_CLI,RESISTANT,RESISTANCE,CLI,80.0,80.0
ermA_SDS,ermA_CLI,RESISTANT,RESISTANCE,CLI,80.0,95.0
ermB,ermB,ERY,RESISTANT,RESISTANCE,80.0,80.0
ermC,ermC_CLI,RESISTANT,RESISTANCE,CLI,80.0,80.0
ermC_LP,ermC_CLI,RESISTANT,MODIFIES_INDUCED,CLI,90.0,95.0
```

### SNPAR (_ar_snps.csv, ar_snps.fa_)
The SNPAR CSV file specifies the genes and variants that confer mutation-based resistance.

The following extra columns are required:

* `Mutation Type`
  * `SAP` - an amino acid polymorphism (the coordinate must correspond to the translated sequence and not the gene sequence). These are normally used for protein-encoding genes
  * `SNP` - a nucleotide polymorphism. These are used for untranslated genes.
* `Mutation` - The type of mutation using the standard nomenclature of {wt}{location}{mutant}, e.g T32S.

To indicate a deletion use the `--57RP` convention. Conversely an insert would be `RP57--`.

The FASTA contains the _DNA_ sequence of the resistance gene. The header must match the corresponding `Gene Name` field.

```
Mutation Type,Resistance Group,Mutation,Gene Name,Resistance Profile,Set Effect,Effect
SAP,,L421P,ponA1,PEN,INTERMEDIATE_NOT_ADDITIVE,RESISTANCE
SAP,,S91F,gyrA,CIP,RESISTANT,RESISTANCE
SAP,,D95N,gyrA,CIP,RESISTANT,RESISTANCE
SAP,folA_57RP,--57RP,folP_AE007317,SSS,RESISTANT,RESISTANCE
```

