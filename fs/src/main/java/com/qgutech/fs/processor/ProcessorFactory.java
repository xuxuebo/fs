package com.qgutech.fs.processor;


import com.qgutech.fs.domain.ProcessorTypeEnum;

public class ProcessorFactory {

    private Processor videoProcessor;
    private Processor audioProcessor;
    private Processor imageProcessor;
    private Processor docProcessor;
    private Processor fileProcessor;
    private Processor zipProcessor;
    private Processor zipVideoProcessor;
    private Processor zipAudioProcessor;
    private Processor zipImageProcessor;
    private Processor zipDocProcessor;

    public Processor acquireProcessor(ProcessorTypeEnum processor) {
        switch (processor) {
            case VID:
                return videoProcessor;
            case AUD:
                return audioProcessor;
            case IMG:
                return imageProcessor;
            case DOC:
                return docProcessor;
            case FILE:
                return fileProcessor;
            case ZIP:
                return zipProcessor;
            case ZVID:
                return zipVideoProcessor;
            case ZAUD:
                return zipAudioProcessor;
            case ZIMG:
                return zipImageProcessor;
            case ZDOC:
                return zipDocProcessor;
            default:
                throw new RuntimeException("This processor[" + processor + "] is not supported!");
        }
    }


    public Processor getVideoProcessor() {
        return videoProcessor;
    }

    public void setVideoProcessor(Processor videoProcessor) {
        this.videoProcessor = videoProcessor;
    }

    public Processor getAudioProcessor() {
        return audioProcessor;
    }

    public void setAudioProcessor(Processor audioProcessor) {
        this.audioProcessor = audioProcessor;
    }

    public Processor getImageProcessor() {
        return imageProcessor;
    }

    public void setImageProcessor(Processor imageProcessor) {
        this.imageProcessor = imageProcessor;
    }

    public Processor getDocProcessor() {
        return docProcessor;
    }

    public void setDocProcessor(Processor docProcessor) {
        this.docProcessor = docProcessor;
    }

    public Processor getFileProcessor() {
        return fileProcessor;
    }

    public void setFileProcessor(Processor fileProcessor) {
        this.fileProcessor = fileProcessor;
    }

    public Processor getZipProcessor() {
        return zipProcessor;
    }

    public void setZipProcessor(Processor zipProcessor) {
        this.zipProcessor = zipProcessor;
    }

    public Processor getZipVideoProcessor() {
        return zipVideoProcessor;
    }

    public void setZipVideoProcessor(Processor zipVideoProcessor) {
        this.zipVideoProcessor = zipVideoProcessor;
    }

    public Processor getZipAudioProcessor() {
        return zipAudioProcessor;
    }

    public void setZipAudioProcessor(Processor zipAudioProcessor) {
        this.zipAudioProcessor = zipAudioProcessor;
    }

    public Processor getZipImageProcessor() {
        return zipImageProcessor;
    }

    public void setZipImageProcessor(Processor zipImageProcessor) {
        this.zipImageProcessor = zipImageProcessor;
    }

    public Processor getZipDocProcessor() {
        return zipDocProcessor;
    }

    public void setZipDocProcessor(Processor zipDocProcessor) {
        this.zipDocProcessor = zipDocProcessor;
    }
}
