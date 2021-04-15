package net.cgps.wgsa.paarsnp.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.cgps.wgsa.paarsnp.core.lib.Jsonnable;

public interface ResultJson extends Jsonnable {
  @JsonInclude(JsonInclude.Include.NON_NULL)
  String getAssemblyId();
  void unsetAssemblyId();
}
