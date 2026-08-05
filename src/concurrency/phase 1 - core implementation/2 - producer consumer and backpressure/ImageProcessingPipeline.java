/*
Problem Statement
Design an image processing system where users upload images that must pass through multiple
processing stages before being stored.

Pipeline:
Resize Image
Apply Watermark
Compress Image
Store Image

Requirements:
Each stage should work independently.
Multiple images should be processed concurrently.
Slow stages should not block other stages.
Preserve processing order for each image.
Support graceful shutdown.

Pattern
Multi-stage Processing Pipeline

Production-grade Approach
Use one BlockingQueue<Image> between every processing stage. Each stage runs as an independent
worker (or worker pool), allowing CPU-intensive stages to scale horizontally.
*/

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ImageProcessingPipeline {

    static class Image {

        final int id;
        final String name;

        Image(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private static final Image POISON = new Image(-1, "POISON");

    private final BlockingQueue<Image> resizeQueue;
    private final BlockingQueue<Image> watermarkQueue;
    private final BlockingQueue<Image> compressQueue;

    ImageProcessingPipeline(int capacity) {
        resizeQueue = new ArrayBlockingQueue<>(capacity);
        watermarkQueue = new ArrayBlockingQueue<>(capacity);
        compressQueue = new ArrayBlockingQueue<>(capacity);
    }

    public void upload(Image image) throws InterruptedException {
        resizeQueue.put(image);
    }

    // resizeImage(Image image)
    public void resize() {
        try {
            while (true) {
                Image image = resizeQueue.take();

                if (image == POISON) {
                    watermarkQueue.put(POISON);
                    break;
                }

                System.out.println("Resize      : " + image.name);

                watermarkQueue.put(image);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // applyWatermark()
    public void watermark() {
        try {
            while (true) {
                Image image = watermarkQueue.take();

                if (image == POISON) {
                    compressQueue.put(POISON);
                    break;
                }

                System.out.println("Watermark  : " + image.name);

                compressQueue.put(image);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // compressImage()
    public void compress() {
        try {
            while (true) {
                Image image = compressQueue.take();

                if (image == POISON) break;

                System.out.println("Compress   : " + image.name);

                store(image);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // storeImage()
    private void store(Image image) {
        System.out.println("Store      : " + image.name);
        System.out.println();
    }

    public static void main(String[] args) throws InterruptedException {
        ImageProcessingPipeline pipeline = new ImageProcessingPipeline(10);

        Thread resize = new Thread(pipeline::resize);
        Thread watermark = new Thread(pipeline::watermark);
        Thread compress = new Thread(pipeline::compress);

        resize.start();
        watermark.start();
        compress.start();

        for (int i = 1; i <= 5; i++) {
            pipeline.upload(new Image(i, "Image-" + i));
        }

        pipeline.upload(POISON);

        resize.join();
        watermark.join();
        compress.join();
    }
}
