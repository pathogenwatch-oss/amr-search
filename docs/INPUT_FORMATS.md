# PAARSNP Input Format

For the current databases look in the [resources directory](../resources)

## Directory Structure

```
/resources/{library-name}.toml
```  
`{library-name}` can be any string, but only NCBI taxon IDs can be bound to the taxon map.   

e.g. 
- `1280.toml` provides the library for _Staphylococcus aureus_,
- `gram_neg_esbl` provides a general library of genes for use in other libraries.

## Format Outline

The TOML files consist of 5 main fields. All fields except `label` are optional in any given file.

1. `label` - [Required] The library name
1. `extends` - Other libraries to inherit from. For more details see below.
1. `antimicrobials` - The list of antimicrobials.
1. `genes` - The AMR-associated genes.
1. `paar` - Resistance based on presence/absence of the genes.
1. `snpar` - Resistance based on gene variants.

### A simple example

```
# Comments are allowed in the file like this.
# Staph aureus example
label = "1280"

# It's possible to include another library by name.
# For details on how they are merged, see the detailed description of `extends`.
# extend = ["another_library"]

antimicrobials = [
{key = "AMI", type = "Aminoglycosides", name = "Amikacin"},
{key = "GEN", type = "Aminoglycosides", name = "Gentamicin"},
{key = "TOB", type = "Aminoglycosides", name = "Tobramycin"},
{key = "KAN", type = "Aminoglycosides", name = "Kanamycin"},
{key = "CIP", type = "Fluoroquinolones", name = "Ciprofloxacin"},
]

genes = [
{name = "aphA-3", pid = 80.0, coverage = 75.0, sequence = "ATGAGAA..."},
{name = "aadD", pid = 80.0, coverage = 75.0, sequence = "ATGAGAATA..."},
{name = "aacA-aphD", pid = 80.0, coverage = 75.0, sequence = "ATGA..."},
{name = "grlA", pid = 80.0, coverage = 75.0, sequence = "ATGAGTGAA..."},
]

paar = [
{phenotypes = [{effect = "RESISTANT", profile = ["TOB","AMI","KAN"]}], members = ["aphA-3"]},
{phenotypes = [{effect = "RESISTANT", profile = ["AMI","TOB","KAN"]}], members = ["aadD"]},
{phenotypes = [{effect = "RESISTANT", profile = ["TOB","GEN","KAN"]}], members = ["aacA-aphD"]},
]

snpar = [
{phenotypes = [{effect = "RESISTANT", profile = ["CIP"]}], members = [{gene="grlA", variants=["S80F"]}]},
{phenotypes = [{effect = "RESISTANT", profile = ["CIP"]}], members = [{gene="grlA", variants=["S80Y"]}]},
]
```
 
## Detailed Description

### Label

The name of the library. If this is an NCBI numeric ID, it will be used as the representative library for that part of the tree.
If the taxon rank of the ID is higher than genus, it can be run using any of the children genus IDs as well.

e.g. 
1. "1280" will only run for _S. aureus_, 
2. "570" will run for all Klebsiella, 
3. "1224" will run with any genus ID from Proteobacteria
NB A library labelled "570" will take precendence over one generated from "1224"

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
* `Resistance Profile` - A comma-separated list of antimicrobials using the name from the first column of the _ar_agents.csv_. If more than one antimicrobial is specified the field must be enclosed in quotes, e.g. `"MET,PEN"`.

Note that the order of the fields in the PAAR and SNPAR CSV files is not important.

### PAAR (_resistance_genes.csv, resistance_genes.fa_)

The PAAR CSV file specifies the resistance genes for a species. The following additional columns to the ones specified above are required:
* `PID Threshold` - The minimum percent identity of the match as reported by BLAST.
* `Coverage Threshold` - The minimum coverage of the reference sequence by the BLAST match.

The FASTA contains the _DNA_ sequence of the resistance gene. The header must match the corresponding `Gene Name` field.

For a simple example that will cover most cases look at [_Neisseria gonorrhoeae_](../resources/485/resistance_genes.csv).

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

