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

### label

The name of the library. If this is an NCBI numeric ID, it will be used as the representative library for that part of the tree.
If the taxon rank of the ID is higher than genus, it can be run using any of the children genus IDs as well.

e.g. 
1. "1280" will only run for _S. aureus_, 
1. "570" will run for all Klebsiella, 
1. "1224" will run with any genus ID from Proteobacteria
NB A library labelled "570" will take precendence over one generated from "1224"

```
# Staph aureus example
label = "1280"

# Default library for Proteobacteria
label = "1224"

# ESBL library
label = "gram_neg_esbl"
```

### antimicrobials

This field contains a list of one or more antimicrobials or broad spectrum resistance mechanisms, such as porin deletion.

Three fields are required for each antimicrobial:

1. `key` - For antimicrobials use the three letter code from <a href="http://www.bsacsurv.org/science/antimicrobials/"> this list</a>.
1. `type` - Typically this is the class of antibiotic, e.g. "Aminoglycosides".
1. `name` - The standard name of the antibiotic or resistance mechanism.

```
antimicrobials = [

# Standard representation
{key = "AMI", type = "Aminoglycosides", name = "Amikacin"},

# Porin gene
{key = "POA", type = "Pores", name = "Pore A"}
]
```

### genes 

The `genes` field contains a list of one or more reference sequences, typically a gene, though other examples include leader peptides and promotor regions. 

Each reference sequence is described with four required fields:

1. `name` - the name of the reference sequence.
1. `pid` - the minimum percent identity of any match in the query assembly.
1. `coverage` - the minimum coverage of the reference sequence by a match in the query assembly. This value is a trade-off between allowing for assembly errors and detecting the presence of pseudogenisation or other disruption.
1. `sequence` - the DNA sequence of the reference.
 
```
genes = [

# Standard gene representation
{name = "aphA-3", pid = 80.0, coverage = 75.0, sequence = "ATGAGAA..."},

# Exact allele required
{name = "blaCTX-M-142_1", pid = 100.0, coverage = 100.0, sequence = "ATGG..."},
]
```

### Basic paar/snpar

The gene presence-absence and variance descriptions are almost identical, and so are descibed together here.

A simple resistance record consists of a single gene or SNP that confers resistance to one or more antimicrobials. A more complex one might consist of multiple required variants, or provide different levels of resistance to different antimicrobials.

Each record consists of two required fields and one optional field:

1. `name` - [Optional] A name for the resistance record. Normally this is omitted and generated automatically by PAARSNP.
1. `phenotypes` - [Required] A list of one or more phenotype records (described below).
1. `members` - [Required] A list of one or more genes or SNP records (described below). 

*Note*: for clarity the records are split over multiple lines in the following examples. These need to be joined into a single line in the final TOML file.

_Simple gene presence-absence_

The presence of aphA-3 confers resistance to Tobramycin, Amikacin & Kanamycin. 
```
{
phenotypes = [{
  effect = "RESISTANT", 
  profile = ["TOB","AMI","KAN"]
  }],
members = ["aphA-3"]
}
```

_Multiple gene presence-absence_

If both sul1 and dfrA7 are present, then Co-Trimoxazole resistance is conferred. Note that if either were required, these would be descibed in two different records.
```
{
phenotypes = [{
  effect = "RESISTANT", 
  profile = ["SXT"]
  }],
members = ["sul1", "dfrA7"]
}
```

_Simple SNP presence-absence_

The S80F (serine -> phenylalanine at position 80) in grlA confers resistance to Ciprofloxacin.
```
{
phenotypes = [{
  effect = "RESISTANT", 
  profile = ["CIP"]
  }], 
members = [{
  gene = "grlA", 
  variants = ["S80F"]
  }]
}
```

_Multiple Variant presence-absence_

If rpoB contains the D435G and L452P variant then Rifampicin resistance is conferred.
```
{
phenotypes = [{
  effect = "RESISTANT", 
  profile = ["RIF"]
}],
members = [{
  gene = "rpoB", 
  variants = ["D435G", "L452P"]
  }]
},
```

### Full paar/snpar Description

It's best to understand the examples above before reading this bit.

#### members

When searching a query assembly, the resistance record is only considered as completely found when all members are identified, otherwise it is marked as "partial" or "not present". Most phenotypes "effect" types require all members to be present, with the exception of "Intermediate_Additive" (see below in "phentoypes").

The members field is straightforward for gene presence-absence, and is simply a list of gene identifiers.

```
members = ["geneA", "geneB", ...]
```

For snpar, the members field consists of a list of variant records. Each record consists of two fields:

1. `name` - the name of the gene containing the variants.
1. `variants` - a list of variant sites that must be present.
    1. Amino acid substitutions use the standard amino acid codes and _must_ be in _upper case_.
        * e.g. leucine to valine at position 72 in the protein = `L72V` (_not_ `l72v`).
        * Only the mutation is actually checked by PAARSNP and not the original sequence.
    1. Nucleotide substitutions take the same format as amino acid substitutions, but are in _lower case_.
        * e.g. cytosine to adenine at position 178 = `c178a` (_not_ `C178A`).
    1. Deletions or inserts are specified using a '-' character on one side.
        * e.g. A deletion at position 15 in an rRNA = `t15-`.
        * e.g. An insert of a glutamate at 73 = `-73E`.
    1. Truncation by premature stop codon can be specified using `truncated`.

```
# Two variant sites are tested from geneA, and a deletion in geneB
members = [{
  gene = "geneA",
  variants = ["C100T", "T200C"]
  },{
  gene = "geneB",
  variants = ["c50-"]
}]

# Truncation of this gene provides resistance
members = [{
  gene = "geneA",
  variants = ["truncated"]
}]
```

#### phenotypes

The phenotypes field allows description of the various phenotypes generated by the specified genotype. Each phenotype record consists of 3 fields, one of which is optional.

1. `effect` - [REQUIRED] accepts the following values:
    * `RESISTANT` - confers resistance if all members are present.
    * `INTERMEDIATE` - confers intermediate resistance if all members are present.
    * `INTERMEDIATE_ADDITIVE` - confers intermediate resistance if at least one member is present and (expected) full resistance if all are.
1. `profile` - [REQUIRED] is the list of antimicrobial keys for the resistance phenotype
1. `modifiers` - [OPTIONAL] is a list of modifier records, each consisting of reference sequence name and effect.  These change the phenotype effect if present. _NB_ this field currently only supports sequence presence/absence, not variants.
    1.  `effect` - [REQUIRED] and accepts the following values.
        * `SUPPRESSES` - if present it suppresses the resistance phenotype.
        * `INDUCED` - if present it changes the phenotype (i.e. gene expression) from constitutive to inducible.
    1. `name` - [REQUIRED] the reference sequence name

```
# Single genotype with different phenotypes for different antimicrobials
phenotypes = {
  effect = "RESISTANT",
  profile = ["AAA", "BBB"]
  },{
  effect = "INTERMEDIATE",
  profile = ["CCC"]
  }
}

# modified phenotype example
{
effect = "RESISTANT",
profile = ["AAA"],
modifiers = [{
  name = "seqA",
  effect = "INDUCIBLE"
  }]
}
```

 
### extends [Advanced]

The `extends` field allows the import of other libraries into the current one. These are read in the order given prior to importing the current file. If two records have the same name then newer record updates the older one according to the rules in "Merging" below.

Examples of use:
```
1224.toml ->
# The Proteobacteria default library is composed of three separate libraries for ease of maintenance. 
label = "1224"
extends = ["gram_neg_carbapenemases", "gram_neg_esbl", "gram_neg_colistin"]


570.toml ->
# The Klebsiella default library is built from the proteobacteria library.
label = "570"
extends = ["1224"]

```

#### Merging Rules


1. In all cases new records (according to the appropriate identifying field) are simply added to the inherited list.
1. `antimicrobials`
    * Records are identified by `key`, other fields are ignored.
    * Duplicates are overwritten with the new attributes (e.g. different `type`).
1. `genes`
    * Records are identified by `name`.
    * Duplicates are overwritten with the new attributes (e.g. different `pid`).
1. `paar/snpar`
    * Records are identified by `name` (optional field, automatically defined using the members field).
    * If a new phenotype record contains an antimicrobial `key` in its `profile` field also present in a previous phenotype, the previous phenotype is removed.
    * `members` should never change.
    