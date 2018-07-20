package net.cgps.wgsa.paarsnp.core.lib.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CodonTable {

  private final Map<String, Character> map;


  public CodonTable() {

    this.map = this.makeCodonMap();
  }

  private Map<String, Character> makeCodonMap() {

    final Map<String, Character> map = new HashMap<>(40);
    map.put("TTT", 'F');
    map.put("TTC", 'F');
    map.put("TTA", 'L');
    map.put("TTG", 'L');
    map.put("CTT", 'L');
    map.put("CTC", 'L');
    map.put("CTA", 'L');
    map.put("CTG", 'L');
    map.put("ATT", 'I');
    map.put("ATC", 'I');
    map.put("ATA", 'I');
    map.put("ATG", 'M');
    map.put("GTT", 'V');
    map.put("GTC", 'V');
    map.put("GTA", 'V');
    map.put("GTG", 'V');
    map.put("TCT", 'S');
    map.put("TCC", 'S');
    map.put("TCA", 'S');
    map.put("TCG", 'S');
    map.put("CCT", 'P');
    map.put("CCC", 'P');
    map.put("CCA", 'P');
    map.put("CCG", 'P');
    map.put("ACT", 'T');
    map.put("ACC", 'T');
    map.put("ACA", 'T');
    map.put("ACG", 'T');
    map.put("GCT", 'A');
    map.put("GCC", 'A');
    map.put("GCA", 'A');
    map.put("GCG", 'A');
    map.put("TAT", 'Y');
    map.put("TAC", 'Y');
    map.put("TAA", '*');
    map.put("TAG", '*');
    map.put("CAT", 'H');
    map.put("CAC", 'H');
    map.put("CAA", 'Q');
    map.put("CAG", 'Q');
    map.put("AAT", 'N');
    map.put("AAC", 'N');
    map.put("AAA", 'K');
    map.put("AAG", 'K');
    map.put("GAT", 'D');
    map.put("GAC", 'D');
    map.put("GAA", 'E');
    map.put("GAG", 'E');
    map.put("TGT", 'C');
    map.put("TGC", 'C');
    map.put("TGA", '*');
    map.put("TGG", 'W');
    map.put("CGT", 'R');
    map.put("CGC", 'R');
    map.put("CGA", 'R');
    map.put("CGG", 'R');
    map.put("AGT", 'S');
    map.put("AGC", 'S');
    map.put("AGA", 'R');
    map.put("AGG", 'R');
    map.put("GGT", 'G');
    map.put("GGC", 'G');
    map.put("GGA", 'G');
    map.put("GGG", 'G');

    return map;
  }

  public Optional<Character> translateCodon(final String codon) {

    return Optional.ofNullable(this.map.get(codon));
  }
}
