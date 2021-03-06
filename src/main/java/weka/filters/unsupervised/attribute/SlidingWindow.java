package weka.filters.unsupervised.attribute;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import weka.core.SingleIndex;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Option;
import weka.core.OptionMetadata;
import weka.filters.SimpleBatchFilter;
import weka.core.Utils;

/**
 * <!-- globalinfo-start --> Counts the amino acids in a protein sequence. 
 * Optionally scales counts to unit sum.
 * <p>
 * <!-- globalinfo-end -->
 * 
 * <!-- options-start --> Valid options are:
 * <pre>
 * -R &lt;index&gt;
 *  Sets the index of the string attribute.
 * </pre>
 * 
 * <pre>
 * -S
 *  Scales counts to sum to unity.
 * </pre>
 * <!-- options-end -->
 * 
 * @author danielhogan
 * @version $Revision$
 */
public class SlidingWindow extends ProteinFilter {
  private static final long serialVersionUID = 1L;

  protected int windowWidth = 3;

  @OptionMetadata(displayName = "Window width", description = "Width of the sliding window.", 
      commandLineParamName = "W", commandLineParamSynopsis = "-W <width>", displayOrder = 1)
  public int getWindowWidth() {
    return windowWidth;
  }

  public void setWindowWidth(String arg) {
    windowWidth = Integer.parseInt(arg);
  }

  @Override
  public Enumeration<Option> listOptions() {
    Vector<Option> options = new Vector<Option>();
    options.add(new Option(
          "\tSet the width of the sliding window.", "W", 1, 
          "-W <width>"
          ));
    options.addAll(Collections.list(super.listOptions()));
    return options.elements();
  }

  @Override
  public String[] getOptions() {
    Vector<String> options = new Vector<String>();
    options.add("-W");
    options.add("" + getWindowWidth());
    Collections.addAll(options, super.getOptions());
    return options.toArray(new String[options.size()]);
  }

  @Override
  public void setOptions(String[] options) throws Exception {
    String arg = Utils.getOption('W', options);
    if (arg.length() != 0) {
      setWindowWidth(arg);
    } else {
      setWindowWidth("3");
    }
    super.setOptions(options);
    Utils.checkForRemainingOptions(options);
  }

  @Override
  public String globalInfo() {
    return "A sliding window filter.";
  }

  @Override
  protected Instances prepareOutputFormat(Instances inputFormat) throws Exception {
    attrIndex.setUpper(inputFormat.numAttributes() - 1);
    int seqAttrIndex = attrIndex.getIndex();
    if (!inputFormat.attribute(seqAttrIndex).isString()) {
      throw new Exception("Specified attribute index (" + seqAttrIndex + ") must be string type.");
    }
    return inputFormat;
  }

  @Override
  protected Instances process(Instances instances) throws Exception {
    Instances output = new Instances(instances, instances.numInstances());
    double[] vals = new double[instances.numAttributes()];
    for (int i = 0; i < instances.numInstances(); i++) {
      String sequence = instances.get(i).toString(attrIndex.getIndex());
      for (int j = 0; j < instances.numAttributes(); j++) {
        if (instances.attribute(j).isString()) {
          // TODO: handle nominal and relational
          vals[j] = output.attribute(j).addStringValue(instances.get(i).toString(j));
        } else {
          vals[j] = instances.get(i).value(j);    
        }
      }
      for (int j = 0; j < sequence.length() - (getWindowWidth()-1); j++) {
        String substr = sequence.substring(j, j+(getWindowWidth()));
        vals[attrIndex.getIndex()] = output.attribute(attrIndex.getIndex()).addStringValue(substr);
        double[] valsCopy = new double[vals.length];
        System.arraycopy(vals, 0, valsCopy, 0, vals.length);
        output.add(new DenseInstance(1.0, valsCopy));
      }
    }
    return output;
  }
}
