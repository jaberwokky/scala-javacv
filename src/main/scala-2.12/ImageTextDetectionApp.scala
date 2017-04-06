import java.awt.{Color, Font}
import java.io.File
import javax.imageio.ImageIO

import org.bytedeco.javacpp._
import org.bytedeco.javacpp.opencv_core._

/**
  * Created by andrey on 3/18/17.
  */
object ImageTextDetectionApp extends App {

  def getRecursiveListOfFiles(dir: File): Array[File] = {
    val files = dir.listFiles()
    val images = dir.listFiles.filter(file => Set("jpg", "png", "jpeg").exists(file.getName.toLowerCase().contains))
    images ++ files.filter(_.isDirectory).flatMap(getRecursiveListOfFiles)
  }

  /*val files = getRecursiveListOfFiles(new File("IMG_1695.JPG"))*/
  val files = Array("IMG_1695.JPG", "IMG_2090.JPG")

  files.par.foreach(file => {
    val imageFilename = file
    val mat = opencv_imgcodecs.imread(imageFilename)

    val channels = new MatVector()

    opencv_text.computeNMChannels(mat, channels)

    val appendChannels = new MatVector(channels.size())

    (0 until channels.size().toInt).foreach(ind => {
      channels.get(ind).copyTo(appendChannels.get(ind))
    })

    val oldSize = channels.size().toInt

    channels.resize(channels.size() * 2)

    (0 until oldSize).foreach(ind => {
      val tmp = appendChannels.get(ind)
      bitwise_not(channels.get(ind), tmp)
      channels.put(ind + oldSize, tmp)
    })

    val er_filter1 = opencv_text.createERFilterNM1(
      opencv_text.loadClassifierNM1("trained_classifierNM1.xml"),
      35,
      0.00030f,
      0.3f,
      0.2f,
      true,
      0.5f
    )

    val er_filter2 = opencv_text.createERFilterNM2(
      opencv_text.loadClassifierNM2("trained_classifierNM2.xml"),
      0.5f
    )

    val regions = new opencv_text.ERStatVectorVector(channels.size())

    (0 until channels.size().toInt).foreach(ind => {
      er_filter1.run(channels.get(ind), regions.get(ind))
      er_filter2.run(channels.get(ind), regions.get(ind))
    })

    val regionGroups = new PointVectorVector()
    val groupsBoxes = new RectVector()

    opencv_text.erGrouping(mat, channels, regions, regionGroups, groupsBoxes)

    if (groupsBoxes.size() > 0 ) {
      (0 until groupsBoxes.size().toInt).foreach(ind => {
        opencv_imgproc.rectangle(mat, groupsBoxes.get(ind).tl(), groupsBoxes.get(ind).br(), new Scalar(255))
      })
      opencv_imgcodecs.imwrite("grouping" + file.toString.split("/").lastOption.getOrElse(file.toString), mat)
    }
  })
  println("Here")
}