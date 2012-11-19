package com.heymoose.infrastructure.service.processing;

public final class Processors {

  private Processors() { }

  public static Processor chain(final Processor... processorList) {
    return new Processor() {
      @Override
      public void process(ProcessableData data) {
        for (Processor processor : processorList) processor.process(data);
      }
    };
  }
}
