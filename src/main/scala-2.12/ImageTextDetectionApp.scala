import java.awt.{Color, Font}
import java.io.File
import javax.imageio.ImageIO

import org.bytedeco.javacpp._
import org.bytedeco.javacpp.opencv_core._

/**
  * Created by andrey on 3/18/17.
  */
object ImageTextDetectionApp extends App {

  val imageFilename = "IMG_2090.JPG"
  val mat = opencv_imgcodecs.imread(imageFilename)

  val channels = new MatVector()

  opencv_text.computeNMChannels(mat, channels)

  (0 until channels.size().toInt).foreach(ind => {
    opencv_imgcodecs.imwrite("channelsB" + ind + ".jpg", channels.get(ind))
  })

  val appendChannels = new MatVector(channels.size())

  (0 until channels.size().toInt).foreach(ind => {
    channels.get(ind).copyTo(appendChannels.get(ind))
  })

  (0 until channels.size().toInt).foreach(ind => {
    opencv_imgcodecs.imwrite("channelsA" + ind + ".jpg", channels.get(ind))
    opencv_imgcodecs.imwrite("channelsAppend" + ind + ".jpg", appendChannels.get(ind))
  })

  val oldSize = channels.size().toInt

  channels.resize(channels.size()*2)

  (0 until oldSize).foreach(ind => {
    val tmp = appendChannels.get(ind)
    bitwise_not(channels.get(ind), tmp)
    opencv_imgcodecs.imwrite("subtr" + ind + ".jpg", tmp)
    opencv_imgcodecs.imwrite("channelsAfterBitwise" + ind + ".jpg", channels.get(ind))
    channels.put(ind + oldSize, tmp)
  })

  (0 until channels.size().toInt).foreach(ind => {
    opencv_imgcodecs.imwrite("AllNewChannels" + ind + ".jpg", channels.get(ind))
  })

  val er_filter1 = opencv_text.createERFilterNM1(
    opencv_text.loadClassifierNM1("trained_classifierNM1.xml"),
    16,
    0.00015f,
    0.13f,
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

  (0 until groupsBoxes.size().toInt).foreach(ind => {
    opencv_imgproc.rectangle(mat, groupsBoxes.get(ind).tl(), groupsBoxes.get(ind).br(), new Scalar(255))
  })

  opencv_imgcodecs.imwrite("grouping.jpg", mat)

  println("Here")
}
