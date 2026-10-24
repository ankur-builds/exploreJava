package basics;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SafeStreamer {

    public void streamHugeData(
        Connection conn,
        OutputStream customerStream,
        int reportId
    ) {
        String sql = "SELECT giant_blob_data FROM massive_reports WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reportId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Get a reference to the live stream directly from the database
                    try (
                        InputStream dbStream = rs.getBinaryStream(
                            "giant_blob_data"
                        )
                    ) {
                        // Allocating a tiny 8KB buffer. Memory usage will NEVER exceed this 8KB,
                        // regardless of whether the file is 100MB or 50GB.
                        byte[] buffer = new byte[8192];
                        int bytesRead;

                        while ((bytesRead = dbStream.read(buffer)) != -1) {
                            // If the customer's internet is slow, this write() method will BLOCK.
                            // The virtual thread will immediately unmount from the platform thread,
                            // stopping the database from fetching more data until the customer catches up!
                            customerStream.write(buffer, 0, bytesRead);
                        }
                        customerStream.flush();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Streaming failed: " + e.getMessage());
        }
    }
}
