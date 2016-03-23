package models.hms.util

import models.hms.HmsShow
import org.joda.time.DateTime

/**
  * author: cvandrei
  * since: 2016-03-23
  */
object HmsShowHelper {

  val ID = 1
  val NAME = Some("Wir von hier")
  val UTC_END = DateTime.now
  val DOWNLOAD_URL = Some("http://localhost:9000/folder/file.mp4")
  val CHANNEL_ID = 1L
  val PARENT_ID = ID

  def defaultObject: HmsShow = HmsShow(ID, NAME, UTC_END, DOWNLOAD_URL, CHANNEL_ID, PARENT_ID)

  def defaultMinimumObject: HmsShow = HmsShow(ID, None, UTC_END, None, CHANNEL_ID, PARENT_ID)

  def defaultJson: String =
    s"""{
        |  "ID": $ID,
        |  "Name": "${NAME.get}",
        |  "Slug": "${NAME.get}",
        |  "UTCBegin": "2016-02-18T09:14:26.800Z",
        |  "UTCEnd": "2016-02-18T09:59:19.880Z",
        |  "ChannelID": $CHANNEL_ID,
        |  "Created": "2016-02-17T09:54:22.440Z",
        |  "CreatedBy": "Admin",
        |  "Modified": "2016-02-17T09:54:22.440Z",
        |  "ModifiedBy": "Admin",
        |  "Category": "",
        |  "ParentID": $PARENT_ID,
        |  "SA_INGEST": false,
        |  "SA_REGISTERINGEST": false,
        |  "SA_RELEASED": false,
        |  "ThirdPartyID": "",
        |  "Color": 536870911,
        |  "DownloadURL": "${DOWNLOAD_URL.get}"
        |}
     """.stripMargin

  def defaultMinimumJson: String =
    s"""{
        |  "ID": $ID,
        |  "Slug": "${NAME.get}",
        |  "UTCBegin": "2016-02-18T09:14:26.800Z",
        |  "UTCEnd": "2016-02-18T09:59:19.880Z",
        |  "ChannelID": $CHANNEL_ID,
        |  "Created": "2016-02-17T09:54:22.440Z",
        |  "CreatedBy": "Admin",
        |  "Modified": "2016-02-17T09:54:22.440Z",
        |  "ModifiedBy": "Admin",
        |  "Category": "",
        |  "ParentID": $PARENT_ID,
        |  "SA_INGEST": false,
        |  "SA_REGISTERINGEST": false,
        |  "SA_RELEASED": false,
        |  "ThirdPartyID": "",
        |  "Color": 536870911
        |}
     """.stripMargin

}
